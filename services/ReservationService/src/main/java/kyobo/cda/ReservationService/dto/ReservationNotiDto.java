package kyobo.cda.ReservationService.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReservationNotiDto {

    private UUID reservationId;
    private String restaurantName;
    private String userEmail;
    private LocalDateTime reservationDateTime;
    private int numberOfGuests;
}
