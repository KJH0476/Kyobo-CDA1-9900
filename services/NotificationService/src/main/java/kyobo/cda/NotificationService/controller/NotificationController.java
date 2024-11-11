package kyobo.cda.NotificationService.controller;

import kyobo.cda.NotificationService.dto.ReservationRequestDto;
import kyobo.cda.NotificationService.dto.WaitListDto;
import kyobo.cda.NotificationService.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import software.amazon.awssdk.services.ses.model.SesException;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @PostMapping("/notification/confirm")
    public ResponseEntity<String> confirmReservation(@RequestBody ReservationRequestDto reservationRequestDto) {
        log.info("{} 예약 확정 메일 전송", reservationRequestDto.getUserEmail());
        notificationService.sendReservationConfirmEmail(reservationRequestDto);

        return new ResponseEntity<>("예약 확정 메일 전송 완료", HttpStatus.OK);
    }

    @PostMapping("/notification/cancel")
    public ResponseEntity<String> cancelReservation(@RequestBody ReservationRequestDto reservationRequestDto) {
        log.info("{} 예약 취소 메일 전송", reservationRequestDto.getUserEmail());
        notificationService.sendReservationCancelEmail(reservationRequestDto);

        return new ResponseEntity<>("예약 취소 메일 전송 완료", HttpStatus.OK);
    }

    @PostMapping("/notification/waiting")
    public ResponseEntity<String> waitingReservation(@RequestBody List<WaitListDto> waitListDtoList) {
        log.info("예약 대기 알림 전송");
        notificationService.sendWaitingReservationEmail(waitListDtoList);

        return new ResponseEntity<>("예약 대기자들에게 알림 전송 완료", HttpStatus.OK);
    }

    @ExceptionHandler(SesException.class)
    public ResponseEntity<String> handleSesException(SesException e) {
        log.error("이메일 전송 실패, SES 오류 발생 {}", e.getMessage());
        return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
    }
}
