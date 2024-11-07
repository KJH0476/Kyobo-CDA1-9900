package kyobo.cda.ReservationService.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AvailabilityTimeResponseDto {

    private int statusCode;
    private String message;
    private List<AvailabilityTimeDto> availabilityTimeList;
}
