package kyobo.cda.ReservationService.controller;

import kyobo.cda.ReservationService.dto.*;
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
    public ResponseEntity<ReservationResponseDto> createReservation(@RequestBody ReservationRequestDto request) {
        ReservationDto reservationDto = reservationService.createReservation(request);
        return new ResponseEntity<>(ReservationResponseDto.builder()
                .statusCode(HttpStatus.CREATED.value())
                .message("예약이 생성되었습니다.")
                .reservationDto(reservationDto)
                .build(), HttpStatus.CREATED);
    }

    // 사용자 별 예약 조회
    @GetMapping("/{email}")
    public ResponseEntity<List<ReservationDto>> getReservations(@PathVariable String email) {
        List<ReservationDto> reservationsByEmail = reservationService.getReservationsByEmail(email);
        return new ResponseEntity<>(reservationsByEmail, HttpStatus.OK);
    }

    // 예약 취소
    @DeleteMapping("/{reservationId}/cancel")
    public ResponseEntity<ReservationResponseDto> cancelReservation(@RequestBody ReservationRequestDto request, UUID reservationId) {
        reservationService.cancelReservation(request, reservationId);
        return new ResponseEntity<>(ReservationResponseDto.builder()
                .statusCode(HttpStatus.OK.value())
                .message("예약이 취소되었습니다.")
                .build(), HttpStatus.OK);
    }

    // 예약 대기 리스트 등록
    @PostMapping("/waiting")
    public ResponseEntity<WaitListResponseDto> registerWaitList(@RequestBody ReservationRequestDto request) {
        WaitListDto waitListDto = reservationService.registerWaitList(request);
        return new ResponseEntity<>(WaitListResponseDto.builder()
                .statusCode(HttpStatus.CREATED.value())
                .message("예약 대기를 등록하였습니다.")
                .waitListDto(waitListDto)
                .build(), HttpStatus.CREATED);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ReservationResponseDto> handleIllegalArgumentException(IllegalArgumentException e) {
        return new ResponseEntity<>(ReservationResponseDto.builder()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .message(e.getMessage())
                .build(), HttpStatus.BAD_REQUEST);
    }
}
