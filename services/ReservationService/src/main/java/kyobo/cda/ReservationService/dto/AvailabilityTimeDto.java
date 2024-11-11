package kyobo.cda.ReservationService.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AvailabilityTimeDto {

    private UUID restaurantId;
    private LocalDate reservationDate;
    private LocalTime reservationTime;
    private int totalTables;
    private int availableTables;
}
