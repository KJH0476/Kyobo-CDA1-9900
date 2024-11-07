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
public class WaitListDto {

    private UUID waitListId;
    private UUID restaurantId;
    private String restaurantName;
    private String userEmail;
    private LocalDateTime reservationDateTime;
}
