package kyobo.cda.NotificationService.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ReservationRequestDto {

    private UUID reservationId;
    private String restaurantName;
    private String userEmail;
    private LocalDateTime reservationDateTime;
    private int numberOfGuests;
}
