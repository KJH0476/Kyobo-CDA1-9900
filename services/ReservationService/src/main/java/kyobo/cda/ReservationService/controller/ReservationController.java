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

    /**
     * 사용자 예약 생성 컨트롤러이다.
     * 사용자의 예약을 생성하고, 결과를 반환한다.
     *
     * @param request 예약 생성 요청 정보
     * @return ResponseEntity<ReservationResponseDto> 예약 생성 결과 반환
     */
    @PostMapping("/create")
    public ResponseEntity<ReservationResponseDto> createReservation(@RequestBody ReservationRequestDto request) {
        ReservationDto reservationDto = reservationService.createReservation(request);
        return new ResponseEntity<>(ReservationResponseDto.builder()
                .statusCode(HttpStatus.CREATED.value())
                .message("예약이 생성되었습니다.")
                .reservationDto(reservationDto)
                .build(), HttpStatus.CREATED);
    }

    /**
     * 사용자의 예약 조회 컨트롤러이다.
     * 사용자의 예약을 조회하고, 결과를 반환한다.
     *
     * @param email 사용자 이메일
     * @return ResponseEntity<List<ReservationDto>> 사용자의 예약 정보 반환
     */
    @GetMapping("/reservations/{email}")
    public ResponseEntity<List<ReservationDto>> getReservations(@PathVariable String email) {
        List<ReservationDto> reservationsByEmail = reservationService.getReservationsByEmail(email);
        return new ResponseEntity<>(reservationsByEmail, HttpStatus.OK);
    }

    /**
     * 사용자의 예약 상세 조회 컨트롤러이다.
     * 사용자의 예약 상세 정보를 조회하고, 결과를 반환한다.
     *
     * @param restaurantId 레스토랑 ID
     * @return ResponseEntity<AvailabilityTimeResponseDto> 예약 가능 시간대 조회 결과 반환
     */
    @GetMapping("/availability/{restaurantId}")
    public ResponseEntity<AvailabilityTimeResponseDto> getAvailableTime(@PathVariable UUID restaurantId) {
        List<AvailabilityTimeDto> availabilityTimeList = reservationService.getAvailableTime(restaurantId);
        return new ResponseEntity<>(AvailabilityTimeResponseDto.builder()
                .statusCode(HttpStatus.OK.value())
                .message("예약 가능 시간대 조회 성공")
                .availabilityTimeList(availabilityTimeList)
                .build(), HttpStatus.OK);
    }

    /**
     * 사용자의 예약 취소 컨트롤러이다.
     * 사용자의 예약을 취소하고, 결과를 반환한다.
     *
     * @param reservationId 예약 ID
     * @return ResponseEntity<ReservationResponseDto> 예약 취소 결과 반환
     */
    @DeleteMapping("/cancel/{reservationId}")
    public ResponseEntity<ReservationResponseDto> cancelReservation(@PathVariable UUID reservationId) {
        reservationService.cancelReservation(reservationId);
        return new ResponseEntity<>(ReservationResponseDto.builder()
                .statusCode(HttpStatus.OK.value())
                .message("예약이 취소되었습니다.")
                .build(), HttpStatus.OK);
    }

    /**
     * 사용자의 예약 대기 등록 컨트롤러이다.
     * 사용자의 예약 대기를 등록하고, 결과를 반환한다.
     *
     * @param request 예약 대기 등록 요청 정보
     * @return ResponseEntity<WaitListResponseDto> 예약 대기 등록 결과 반환
     */
    @PostMapping("/waiting")
    public ResponseEntity<WaitListResponseDto> registerWaitList(@RequestBody ReservationRequestDto request) {
        WaitListDto waitListDto = reservationService.registerWaitList(request);
        return new ResponseEntity<>(WaitListResponseDto.builder()
                .statusCode(HttpStatus.CREATED.value())
                .message("예약 대기를 등록하였습니다.")
                .waitListDto(waitListDto)
                .build(), HttpStatus.CREATED);
    }

    /**
     * 사용자의 예약 대기 조회 컨트롤러이다.
     * 사용자의 예약 대기를 조회하고, 결과를 반환한다.
     *
     * @param e mail 사용자 이메일
     * @return ResponseEntity<List<WaitListDto>> 사용자의 예약 대기 정보 반환
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ReservationResponseDto> handleIllegalArgumentException(IllegalArgumentException e) {
        return new ResponseEntity<>(ReservationResponseDto.builder()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .message(e.getMessage())
                .build(), HttpStatus.BAD_REQUEST);
    }
}
