package kyobo.cda.ReservationService.service;

import kyobo.cda.ReservationService.dto.ReservationDto;
import kyobo.cda.ReservationService.dto.ReservationNotiDto;
import kyobo.cda.ReservationService.dto.ReservationRequestDto;
import kyobo.cda.ReservationService.dto.WaitListDto;
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
import java.util.List;
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
        // 해당 restaurantId와 시간으로 예약 가능 여부를 확인
        RestaurantAvailability availability = restaurantAvailabilityRepository.findByRestaurantIdAndReservationDateAndReservationTime(
                        request.getRestaurantId(), request.getReservationDate(), request.getReservationTime())
                .orElseThrow(() -> new IllegalArgumentException("해당 시간에 예약이 불가능합니다."));

        // 예약 생성
        Reservation reservation = Reservation.builder()
                .restaurantId(request.getRestaurantId())
                .userEmail(request.getUserEmail())
                .availability(availability)
                .reservationDateTime(LocalDateTime.of(request.getReservationDate(), request.getReservationTime()))
                .numberOfGuests(request.getNumberOfGuests())
                .build();

        reservationRepository.save(reservation);

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

        ResponseEntity<String> response = restTemplate.exchange(notificationServerUrl+"/notification/confirm", HttpMethod.POST, entity, String.class);
        log.info("Notification 서버로 예약 정보 전송 완료. 응답: {}", response.getBody());

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

        // 예약 삭제
        reservationRepository.deleteById(reservationId);
        log.info("예약이 삭제되었습니다. 예약 ID: {}", reservationId);

        // 예약 생성 시 Notification 서버로 예약 정보 전송
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<ReservationNotiDto> entity = new HttpEntity<>(ReservationNotiDto.builder()
                .reservationId(reservation.getId())
                .userEmail(reservation.getUserEmail())
                .reservationDateTime(reservation.getReservationDateTime())
                .numberOfGuests(reservation.getNumberOfGuests())
                .build(), headers);

        ResponseEntity<String> response = restTemplate.exchange(notificationServerUrl+"/notification/cancel", HttpMethod.POST, entity, String.class);
        log.info("Notification 서버로 예약 취소 정보 전송 완료. 응답: {}", response.getBody());
    }

    @Transactional
    public List<ReservationDto> getReservationsByEmail(String email) {
        List<Reservation> reservations = reservationRepository.findByUserEmail(email);

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
                    .userEmail(waitList.getUserEmail())
                    .reservationDateTime(LocalDateTime.of(request.getReservationDate(), request.getReservationTime()))
                    .numberOfGuests(waitList.getNumberOfGuests())
                    .build();
        } else {
            throw new IllegalStateException("예약 가능한 테이블이 있어 대기 등록이 불가능합니다.");
        }
    }
}
