package kyobo.cda.ReservationService.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReservationDto {

    private UUID reservationId;
    private UUID restaurantId;
    private String userEmail;
    private LocalDateTime reservationDateTime;
    private int numberOfGuests;
}
