package kyobo.cda.ReservationService.controller;

import kyobo.cda.ReservationService.dto.ReservationDto;
import kyobo.cda.ReservationService.dto.ReservationRequestDto;
import kyobo.cda.ReservationService.dto.WaitListDto;
import kyobo.cda.ReservationService.service.ReservationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;

    // 예약 생성
    @PostMapping("/create")
    public ResponseEntity<ReservationDto> createReservation(@RequestBody ReservationRequestDto request) {
        ReservationDto reservationDto = reservationService.createReservation(request);
        return new ResponseEntity<>(reservationDto, HttpStatus.CREATED);
    }

    // 사용자 별 예약 조회
    @GetMapping("/{email}")
    public ResponseEntity<List<ReservationDto>> getReservations(@PathVariable String email) {
        List<ReservationDto> reservationsByEmail = reservationService.getReservationsByEmail(email);
        return new ResponseEntity<>(reservationsByEmail, HttpStatus.OK);
    }

    // 예약 취소
    @DeleteMapping("/{reservationId}/cancel")
    public ResponseEntity<Void> cancelReservation(@PathVariable UUID reservationId) {
        reservationService.cancelReservation(reservationId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    // 예약 대기 리스트 등록
    @PostMapping("/waiting")
    public ResponseEntity<WaitListDto> registerWaitList(@RequestBody ReservationRequestDto request) {
        WaitListDto waitListDto = reservationService.registerWaitList(request);
        return new ResponseEntity<>(waitListDto, HttpStatus.CREATED);
    }
}
