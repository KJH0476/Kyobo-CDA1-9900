package kyobo.cda.ReservationService.reservation;

import kyobo.cda.ReservationService.dto.AvailabilityTimeDto;
import kyobo.cda.ReservationService.dto.ReservationDto;
import kyobo.cda.ReservationService.dto.ReservationRequestDto;
import kyobo.cda.ReservationService.dto.WaitListDto;
import kyobo.cda.ReservationService.entity.Reservation;
import kyobo.cda.ReservationService.entity.RestaurantAvailability;
import kyobo.cda.ReservationService.entity.WaitList;
import kyobo.cda.ReservationService.repository.ReservationRepository;
import kyobo.cda.ReservationService.repository.RestaurantAvailabilityRepository;
import kyobo.cda.ReservationService.repository.WaitListRepository;
import kyobo.cda.ReservationService.service.ReservationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ReservationTest {

    @InjectMocks
    private ReservationService reservationService;

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private RestaurantAvailabilityRepository restaurantAvailabilityRepository;

    @Mock
    private WaitListRepository waitListRepository;

    @Mock
    private RestTemplate restTemplate;

    private ReservationRequestDto requestDto;
    private Reservation reservation;
    private RestaurantAvailability availability;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(reservationService, "notificationServerUrl", "http://localhost:8082");

        UUID restaurantId = UUID.randomUUID();
        String userEmail = "test@example.com";
        LocalDate reservationDate = LocalDate.now().plusDays(1);
        LocalTime reservationTime = LocalTime.of(12, 0);
        int numberOfGuests = 2;

        requestDto = new ReservationRequestDto(
                restaurantId,
                userEmail,
                reservationDate,
                reservationTime,
                numberOfGuests
        );

        availability = RestaurantAvailability.builder()
                .id(UUID.randomUUID())
                .restaurantId(restaurantId)
                .restaurantName("테스트 레스토랑")
                .reservationDate(reservationDate)
                .reservationTime(reservationTime)
                .totalTables(10)
                .availableTables(5)
                .build();

        reservation = Reservation.builder()
                .id(UUID.randomUUID())
                .restaurantId(restaurantId)
                .userEmail(userEmail)
                .availability(availability)
                .reservationDateTime(LocalDateTime.of(reservationDate, reservationTime))
                .numberOfGuests(numberOfGuests)
                .build();

        ReflectionTestUtils.setField(reservationService, "restTemplate", restTemplate);
    }

    @Test
    void 예약생성_성공_테스트() {
        when(reservationRepository.findByUserEmailAndRestaurantIdAndReservationDateTime(
                anyString(), any(UUID.class), any(LocalDateTime.class)))
                .thenReturn(Optional.empty());

        when(restaurantAvailabilityRepository.findByRestaurantIdAndReservationDateAndReservationTime(
                any(UUID.class), any(LocalDate.class), any(LocalTime.class)))
                .thenReturn(Optional.of(availability));

        UUID reservationId = UUID.fromString("f50c7cf4-8887-41c9-a819-f1bffcf51beb");

        doAnswer(invocation -> {
            Reservation res = invocation.getArgument(0);
            res.setId(reservationId);
            return res;
        }).when(reservationRepository).save(any(Reservation.class));

        when(restTemplate.exchange(anyString(), any(), any(), eq(String.class)))
                .thenReturn(ResponseEntity.ok("Success"));

        ReservationDto result = reservationService.createReservation(requestDto);

        assertNotNull(result);
        assertEquals(reservationId, result.getReservationId());
        assertEquals(requestDto.getUserEmail(), result.getUserEmail());

        assertEquals(4, availability.getAvailableTables());

        verify(reservationRepository, times(1)).save(any(Reservation.class));
        verify(restaurantAvailabilityRepository, times(1)).save(any(RestaurantAvailability.class));
        verify(restTemplate, times(1)).exchange(anyString(), any(), any(), eq(String.class));
    }

    @Test
    void 예약생성_중복예약_테스트() {
        when(reservationRepository.findByUserEmailAndRestaurantIdAndReservationDateTime(
                anyString(), any(UUID.class), any(LocalDateTime.class)))
                .thenReturn(Optional.of(reservation));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            reservationService.createReservation(requestDto);
        });

        assertEquals("이미 해당 시간에 예약이 존재합니다.", exception.getMessage());

        verify(restaurantAvailabilityRepository, times(0)).findByRestaurantIdAndReservationDateAndReservationTime(
                any(UUID.class), any(LocalDate.class), any(LocalTime.class));
    }

    @Test
    void 예약생성_예약가능시간대X_테스트() {
        when(reservationRepository.findByUserEmailAndRestaurantIdAndReservationDateTime(
                anyString(), any(UUID.class), any(LocalDateTime.class)))
                .thenReturn(Optional.empty());

        when(restaurantAvailabilityRepository.findByRestaurantIdAndReservationDateAndReservationTime(
                any(UUID.class), any(LocalDate.class), any(LocalTime.class)))
                .thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            reservationService.createReservation(requestDto);
        });

        assertEquals("해당 시간에 예약이 불가능합니다.", exception.getMessage());
    }

    @Test
    void 예약생성_테이블X_테스트() {
        // 중복 예약 확인: 예약 없음
        when(reservationRepository.findByUserEmailAndRestaurantIdAndReservationDateTime(
                anyString(), any(UUID.class), any(LocalDateTime.class)))
                .thenReturn(Optional.empty());

        // 예약 가능 여부 확인: 가능하지만 테이블 없음
        availability.setAvailableTables(0);
        when(restaurantAvailabilityRepository.findByRestaurantIdAndReservationDateAndReservationTime(
                any(UUID.class), any(LocalDate.class), any(LocalTime.class)))
                .thenReturn(Optional.of(availability));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            reservationService.createReservation(requestDto);
        });

        assertEquals("예약 가능한 자리가 없습니다.", exception.getMessage());
    }

    @Test
    void 예약취소_예약존재X_테스트() {
        UUID reservationId = reservation.getId();

        when(reservationRepository.findById(reservationId))
                .thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            reservationService.cancelReservation(reservationId);
        });

        assertEquals("해당 예약을 찾을 수 없습니다.", exception.getMessage());
    }

    @Test
    void 예약조회_성공_테스트() {
        when(reservationRepository.findByUserEmail(anyString()))
                .thenReturn(Arrays.asList(reservation));

        List<ReservationDto> reservations = reservationService.getReservationsByEmail("test@example.com");

        assertNotNull(reservations);
        assertEquals(1, reservations.size());
        assertEquals(reservation.getId(), reservations.get(0).getReservationId());

        verify(reservationRepository, times(1)).findByUserEmail(anyString());
    }

    @Test
    void 예약조회_예약내역X_테스트() {
        when(reservationRepository.findByUserEmail(anyString()))
                .thenReturn(Collections.emptyList());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            reservationService.getReservationsByEmail("test@example.com");
        });

        assertEquals("예약 내역이 존재하지 않습니다.", exception.getMessage());
    }

    @Test
    void 예약시간조회_성공_테스트() {
        when(restaurantAvailabilityRepository.findByRestaurantId(any(UUID.class)))
                .thenReturn(Arrays.asList(availability));

        List<AvailabilityTimeDto> availableTimes = reservationService.getAvailableTime(UUID.randomUUID());

        assertNotNull(availableTimes);
        assertEquals(1, availableTimes.size());
        assertEquals(availability.getReservationDate(), availableTimes.get(0).getReservationDate());

        verify(restaurantAvailabilityRepository, times(1)).findByRestaurantId(any(UUID.class));
    }

    @Test
    void 예약시간조회_시간대존재X_테스트() {
        when(restaurantAvailabilityRepository.findByRestaurantId(any(UUID.class)))
                .thenReturn(Collections.emptyList());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            reservationService.getAvailableTime(UUID.randomUUID());
        });

        assertEquals("해당 식당의 예약 가능 시간대가 존재하지 않습니다.", exception.getMessage());
    }

    @Test
    void 예약대기등록_성공_테스트() {
        availability.setAvailableTables(0);
        when(restaurantAvailabilityRepository.findByRestaurantIdAndReservationDateAndReservationTime(
                any(UUID.class), any(LocalDate.class), any(LocalTime.class)))
                .thenReturn(Optional.of(availability));

        when(waitListRepository.save(any(WaitList.class))).thenReturn(WaitList.builder()
                .id(UUID.randomUUID())
                .restaurantId(availability.getRestaurantId())
                .userEmail(requestDto.getUserEmail())
                .availability(availability)
                .numberOfGuests(requestDto.getNumberOfGuests())
                .build());

        WaitListDto result = reservationService.registerWaitList(requestDto);

        assertNotNull(result);
        assertEquals(requestDto.getUserEmail(), result.getUserEmail());

        verify(waitListRepository, times(1)).save(any(WaitList.class));
    }

    @Test
    void 예약대기등록_테이블존재_테스트() {
        when(restaurantAvailabilityRepository.findByRestaurantIdAndReservationDateAndReservationTime(
                any(UUID.class), any(LocalDate.class), any(LocalTime.class)))
                .thenReturn(Optional.of(availability));

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            reservationService.registerWaitList(requestDto);
        });

        assertEquals("예약 가능한 테이블이 있어 대기 등록이 불가능합니다.", exception.getMessage());
    }
}
