package kyobo.cda.ReservationService.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WaitListResponseDto {

    private int statusCode;
    private String message;
    private WaitListDto waitListDto;
}
