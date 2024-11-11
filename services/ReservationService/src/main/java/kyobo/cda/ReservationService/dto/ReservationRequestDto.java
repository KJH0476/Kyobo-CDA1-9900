package kyobo.cda.ReservationService.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReservationRequestDto {

    @NotNull
    private UUID restaurantId;

    @NotNull
    private String userEmail;

    @NotNull
    private LocalDate reservationDate;

    @NotNull
    private LocalTime reservationTime;

    @NotNull
    private int numberOfGuests;
}
