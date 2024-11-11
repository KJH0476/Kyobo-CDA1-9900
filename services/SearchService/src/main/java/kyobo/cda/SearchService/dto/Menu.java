package kyobo.cda.SearchService.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Menu {

    private UUID restaurantId;
    private String menuName;
    private int price;
    private String description;
    private String imageUrl;
}
