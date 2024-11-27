package kyobo.cda.ReservationService.service;

import kyobo.cda.ReservationService.dto.*;
import kyobo.cda.ReservationService.entity.Reservation;
import kyobo.cda.ReservationService.entity.RestaurantAvailability;
import kyobo.cda.ReservationService.entity.WaitList;
import kyobo.cda.ReservationService.repository.ReservationRepository;
import kyobo.cda.ReservationService.repository.RestaurantAvailabilityRepository;
import kyobo.cda.ReservationService.repository.WaitListRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReservationService {

    @Value("${notification.server.url}")
    private String notificationServerUrl;
    private final RestTemplate restTemplate = new RestTemplate();
    private final ReservationRepository reservationRepository;
    private final RestaurantAvailabilityRepository restaurantAvailabilityRepository;
    private final WaitListRepository waitListRepository;

    /**
     * 사용자의 예약을 생성하는 메서드이다.
     * 사용자의 예약을 생성하고, 생성된 예약 정보를 반환한다.
     *
     * @param request 예약 생성 요청 정보
     * @return ReservationDto 생성된 예약 정보 반환
     */
    @Transactional
    public ReservationDto createReservation(ReservationRequestDto request) {
        // 중복 예약 확인
        reservationRepository.findByUserEmailAndRestaurantIdAndReservationDateTime(
                request.getUserEmail(),
                request.getRestaurantId(),
                LocalDateTime.of(request.getReservationDate(), request.getReservationTime())
        ).ifPresent(r -> { throw new IllegalArgumentException("이미 해당 시간에 예약이 존재합니다."); });

        // 해당 restaurantId와 시간으로 예약 가능 여부를 확인
        RestaurantAvailability availability = restaurantAvailabilityRepository.findByRestaurantIdAndReservationDateAndReservationTime(
                        request.getRestaurantId(), request.getReservationDate(), request.getReservationTime())
                .orElseThrow(() -> new IllegalArgumentException("해당 시간에 예약이 불가능합니다."));

        // 예약 가능한 테이블이 있는지 확인
        if (availability.getAvailableTables() <= 0) {
            throw new IllegalArgumentException("예약 가능한 자리가 없습니다.");
        }

        // 예약 후 availableTables 감소
        availability.setAvailableTables(availability.getAvailableTables() - 1);
        restaurantAvailabilityRepository.save(availability);

        // 예약 생성
        Reservation reservation = Reservation.builder()
                .restaurantId(request.getRestaurantId())
                .userEmail(request.getUserEmail())
                .restaurantName(availability.getRestaurantName())
                .availability(availability)
                .reservationDateTime(LocalDateTime.of(request.getReservationDate(), request.getReservationTime()))
                .numberOfGuests(request.getNumberOfGuests())
                .build();

        // 예약 정보 저장
        reservationRepository.save(reservation);

        // 예약 정보 저장 성공 시 Notification 서버로 예약 정보 전송
        ResponseEntity<String> response = sendNotification(reservation, "/notification/confirm");
        log.info("Notification 서버로 예약 생성 요청 완료, 응답: {}", response.getBody());

        return ReservationDto.builder()
                .reservationId(reservation.getId())
                .restaurantId(reservation.getRestaurantId())
                .restaurantName(reservation.getRestaurantName())
                .userEmail(reservation.getUserEmail())
                .reservationDateTime(reservation.getReservationDateTime())
                .numberOfGuests(reservation.getNumberOfGuests())
                .build();
    }

    /**
     * 사용자의 예약을 취소하는 메서드이다.
     * 사용자의 예약을 취소하고, 취소된 예약 정보를 반환한다.
     *
     * @param reservationId 예약 ID
     */
    @Transactional
    public void cancelReservation(UUID reservationId) {

        // 예약 정보 조회
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("해당 예약을 찾을 수 없습니다."));

        // 예약 가능 시간대 조회, 예약 가능 테이블 증가
        RestaurantAvailability availability = restaurantAvailabilityRepository.findByRestaurantIdAndReservationDateAndReservationTime(
                        reservation.getRestaurantId(), reservation.getReservationDateTime().toLocalDate(), reservation.getReservationDateTime().toLocalTime())
                .map(restaurantAvailability -> {
                    log.info("예약 취소: {}", reservation);
                    restaurantAvailability.setAvailableTables(restaurantAvailability.getAvailableTables() + 1);
                    return restaurantAvailabilityRepository.save(restaurantAvailability);
                }).orElseThrow(() -> new IllegalArgumentException("해당 예약 시간을 찾을 수 없습니다."));

        // 예약 정보 삭제
        reservationRepository.deleteById(reservationId);
        log.info("예약이 삭제되었습니다. 예약 ID: {}", reservationId);

        // 예약 정보 삭제 성공 시 Notification 서버로 예약 정보 전송
        ResponseEntity<String> response = sendNotification(reservation, "/notification/cancel");
        log.info("Notification 서버로 예약 취소 요청 완료, 응답: {}", response.getBody());

        // 해당 예약 시간의 대기 목록 조회
        List<WaitListDto> waitingInfoList = new ArrayList<>();
        List<WaitList> waitListEntries = waitListRepository.findByAvailability(availability);
        if (!waitListEntries.isEmpty()) {
            log.info("대기 중인 사용자 목록 조회 완료: 총 {}명", waitListEntries.size());
            for (WaitList waitList : waitListEntries) {
                log.info("대기 사용자 이메일: {}", waitList.getUserEmail());
                waitingInfoList.add(WaitListDto.builder()
                        .waitListId(waitList.getId())
                        .restaurantId(waitList.getRestaurantId())
                        .restaurantName(availability.getRestaurantName())
                        .userEmail(waitList.getUserEmail())
                        .reservationDateTime(reservation.getReservationDateTime())
                        .build());
            }

            // 대기 중인 사용자에게 Notification 서버로 새로운 예약 가능 알림 전송
            ResponseEntity<String> waitingNotificationResponse = sendWaitingNotification(waitingInfoList);

            // 기존 대기 목록 삭제
            waitListRepository.deleteAll(waitListEntries);
        } else {
            log.info("해당 시간에 대기 중인 사용자가 없습니다.");
        }
        log.info("예약 취소 완료: {}", reservationId);
    }

    /**
     * 사용자의 예약 내역을 조회하는 메서드이다.
     * 사용자의 이메일로 예약 내역을 조회하고, 조회된 예약 정보를 반환한다.
     *
     * @param email 사용자 이메일
     * @return List<ReservationDto> 사용자의 예약 정보 반환
     */
    @Transactional
    public List<ReservationDto> getReservationsByEmail(String email) {

        // 사용자의 모든 예약 정보 조회
        List<Reservation> reservations = reservationRepository.findByUserEmail(email);

        if (reservations.isEmpty()) {
            throw new IllegalArgumentException("예약 내역이 존재하지 않습니다.");
        }

        return reservations.stream()
                .map(reservation -> {
                    log.info("예약 조회: {}", reservation);
                    return ReservationDto.builder()
                            .reservationId(reservation.getId())
                            .restaurantId(reservation.getRestaurantId())
                            .userEmail(reservation.getUserEmail())
                            .restaurantName(reservation.getRestaurantName())
                            .reservationDateTime(reservation.getReservationDateTime())
                            .numberOfGuests(reservation.getNumberOfGuests())
                            .build();
                }).toList();
    }

    /**
     * 식당 예약 가능 시간대를 조회하는 메서드이다.
     * 사용자가 예약 가능한 시간대를 조회하고, 조회된 식당의 예약 가능 시간대 정보를 반환한다.
     *
     * @param restaurantId 레스토랑 ID
     * @return List<AvailabilityTimeDto> 식당의 예약 가능 시간대 정보 반환
     */
    @Transactional
    public List<AvailabilityTimeDto> getAvailableTime(UUID restaurantId) {

        // 해당 restaurantId의 예약 가능 시간대 조회
        List<RestaurantAvailability> availabilities = restaurantAvailabilityRepository.findByRestaurantId(restaurantId);

        if(availabilities.isEmpty()) {
            throw new IllegalArgumentException("해당 식당의 예약 가능 시간대가 존재하지 않습니다.");
        }

        return availabilities.stream()
                .map(availability -> {
                    log.info("예약 가능 시간대 조회: {}", availability);
                    return AvailabilityTimeDto.builder()
                            .restaurantId(availability.getRestaurantId())
                            .reservationDate(availability.getReservationDate())
                            .reservationTime(availability.getReservationTime())
                            .totalTables(availability.getTotalTables())
                            .availableTables(availability.getAvailableTables())
                            .build();
                }).toList();
    }

    /**
     * 예약 대기를 등록하는 메서드이다.
     * 사용자의 예약 대기를 등록하고, 등록된 대기 정보를 반환한다.
     *
     * @param request 예약 대기 등록 요청 정보
     * @return WaitListDto 등록된 대기 정보 반환
     */
    @Transactional
    public WaitListDto registerWaitList(ReservationRequestDto request) {
        // 해당 restaurantId와 시간으로 예약 가능 여부를 확인
        RestaurantAvailability availability = restaurantAvailabilityRepository.findByRestaurantIdAndReservationDateAndReservationTime(
                        request.getRestaurantId(), request.getReservationDate(), request.getReservationTime())
                .orElseThrow(() -> new IllegalArgumentException("해당 예약 시간을 찾을 수 없습니다."));


        // 남은 테이블이 없는 경우 대기 등록 진행
        if (availability.getAvailableTables() <= 0) {
            WaitList waitList = WaitList.builder()
                    .restaurantId(request.getRestaurantId())
                    .userEmail(request.getUserEmail())
                    .availability(availability)
                    .numberOfGuests(request.getNumberOfGuests())
                    .build();

            waitListRepository.save(waitList);
            log.info("대기 등록이 완료되었습니다. 사용자 Email: {}, 레스토랑 Id: {}", request.getUserEmail(), request.getRestaurantId());

            return WaitListDto.builder()
                    .waitListId(waitList.getId())
                    .restaurantId(waitList.getRestaurantId())
                    .restaurantName(availability.getRestaurantName())
                    .userEmail(waitList.getUserEmail())
                    .reservationDateTime(LocalDateTime.of(request.getReservationDate(), request.getReservationTime()))
                    .build();
        } else {
            throw new IllegalStateException("예약 가능한 테이블이 있어 대기 등록이 불가능합니다.");
        }
    }

    /**
     * Notification 서버로 예약 확정, 취소 정보를 전송하는 메서드이다.
     *
     * @param reservation 예약 정보
     * @param path Notification 서버로 전송할 경로
     * @return ResponseEntity<String> Notification 서버로 전송한 결과 반환
     */
    private ResponseEntity<String> sendNotification(Reservation reservation, String path) {
        // 예약 생성 시 Notification 서버로 예약 정보 전송
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<ReservationNotiDto> entity = new HttpEntity<>(ReservationNotiDto.builder()
                .reservationId(reservation.getId())
                .restaurantName(reservation.getRestaurantName())
                .userEmail(reservation.getUserEmail())
                .reservationDateTime(reservation.getReservationDateTime())
                .numberOfGuests(reservation.getNumberOfGuests())
                .build(), headers);
        return restTemplate.exchange(notificationServerUrl+path, HttpMethod.POST, entity, String.class);
    }

    /**
     * Notification 서버로 대기 정보 전송하는 메서드이다.
     *
     * @param waitingInfoList 대기중인 사용자 리스트
     * @return ResponseEntity<String> Notification 서버로 전송한 결과 반환
     */
    private ResponseEntity<String> sendWaitingNotification(List<WaitListDto> waitingInfoList) {

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<List<WaitListDto>> entity = new HttpEntity<>(waitingInfoList, headers);
        return restTemplate.exchange(notificationServerUrl+"/notification/waiting", HttpMethod.POST, entity, String.class);
    }
}
