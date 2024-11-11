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
                .availability(availability)
                .reservationDateTime(LocalDateTime.of(request.getReservationDate(), request.getReservationTime()))
                .numberOfGuests(request.getNumberOfGuests())
                .build();

        reservationRepository.save(reservation);

        ResponseEntity<String> response = sendNotification(reservation, availability, "/notification/confirm");
        log.info("Notification 서버로 예약 생성 요청 완료, 응답: {}", response.getBody());

        return ReservationDto.builder()
                .reservationId(reservation.getId())
                .restaurantId(reservation.getRestaurantId())
                .userEmail(reservation.getUserEmail())
                .reservationDateTime(reservation.getReservationDateTime())
                .numberOfGuests(reservation.getNumberOfGuests())
                .build();
    }

    // 예약 취소 메서드
    @Transactional
    public void cancelReservation(UUID reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("해당 예약을 찾을 수 없습니다."));

        RestaurantAvailability availability = restaurantAvailabilityRepository.findByRestaurantIdAndReservationDateAndReservationTime(
                        reservation.getRestaurantId(), reservation.getReservationDateTime().toLocalDate(), reservation.getReservationDateTime().toLocalTime())
                .map(restaurantAvailability -> {
                    log.info("예약 취소: {}", reservation);
                    restaurantAvailability.setAvailableTables(restaurantAvailability.getAvailableTables() + 1);
                    return restaurantAvailabilityRepository.save(restaurantAvailability);
                }).orElseThrow(() -> new IllegalArgumentException("해당 예약 시간을 찾을 수 없습니다."));

        // 예약 삭제
        reservationRepository.deleteById(reservationId);
        log.info("예약이 삭제되었습니다. 예약 ID: {}", reservationId);

        ResponseEntity<String> response = sendNotification(reservation, availability, "/notification/cancel");
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

            ResponseEntity<String> waitingNotificationResponse = sendWaitingNotification(waitingInfoList);

            waitListRepository.deleteAll(waitListEntries);
        } else {
            log.info("해당 시간에 대기 중인 사용자가 없습니다.");
        }
        log.info("예약 취소 완료: {}", reservationId);
    }

    @Transactional
    public List<ReservationDto> getReservationsByEmail(String email) {
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
                            .reservationDateTime(reservation.getReservationDateTime())
                            .numberOfGuests(reservation.getNumberOfGuests())
                            .build();
                }).toList();
    }

    // 예약 가능 시간대 조회
    @Transactional
    public List<AvailabilityTimeDto> getAvailableTime(UUID restaurantId) {
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

    // 예약 시간에 대기 등록 메서드
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

    private ResponseEntity<String> sendNotification(Reservation reservation, RestaurantAvailability availability, String path) {
        // 예약 생성 시 Notification 서버로 예약 정보 전송
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<ReservationNotiDto> entity = new HttpEntity<>(ReservationNotiDto.builder()
                .reservationId(reservation.getId())
                .restaurantName(availability.getRestaurantName())
                .userEmail(reservation.getUserEmail())
                .reservationDateTime(reservation.getReservationDateTime())
                .numberOfGuests(reservation.getNumberOfGuests())
                .build(), headers);
        return restTemplate.exchange(notificationServerUrl+path, HttpMethod.POST, entity, String.class);
    }

    private ResponseEntity<String> sendWaitingNotification(List<WaitListDto> waitingInfoList) {

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<List<WaitListDto>> entity = new HttpEntity<>(waitingInfoList, headers);
        return restTemplate.exchange(notificationServerUrl+"/notification/waiting", HttpMethod.POST, entity, String.class);
    }
}
