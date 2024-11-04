package kyobo.cda.NotificationService.controller;

import kyobo.cda.NotificationService.dto.ReservationRequestDto;
import kyobo.cda.NotificationService.dto.ReservationResponseDto;
import kyobo.cda.NotificationService.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @PostMapping("/reservation/confirm")
    public ResponseEntity<ReservationResponseDto> confirmReservation(@RequestBody ReservationRequestDto reservationRequestDto) {
        log.info("{} 예약 확정 메일 전송", reservationRequestDto.getEmail());
        notificationService.sendReservationConfirmEmail(reservationRequestDto);

        return ResponseEntity.ok(ReservationResponseDto.builder()
                .statusCode(HttpStatus.OK.value())
                .message("예약 확정 메일 전송 완료")
                .reservationId(reservationRequestDto.getReservationId())
                .build());
    }

    @PostMapping("/reservation/cancel")
    public ResponseEntity<ReservationResponseDto> cancelReservation(@RequestBody ReservationRequestDto reservationRequestDto) {
        log.info("예약 취소");
        notificationService.sendReservationCancelEmail(reservationRequestDto);

        return ResponseEntity.ok(ReservationResponseDto.builder()
                .statusCode(HttpStatus.OK.value())
                .message("예약 취소 메일 전송 완료")
                .reservationId(reservationRequestDto.getReservationId())
                .build());
    }

    @PostMapping("/reservation/waiting")
    public void waitingReservation() {
        log.info("예약 대기");
    }
}
