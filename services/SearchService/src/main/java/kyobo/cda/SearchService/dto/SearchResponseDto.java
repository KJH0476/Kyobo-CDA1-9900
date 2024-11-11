package kyobo.cda.SearchService.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchResponseDto {

    private int statusCode;
    private String message;
    private List<Restaurants> restaurants;
    private Object[] searchAfter;
}
