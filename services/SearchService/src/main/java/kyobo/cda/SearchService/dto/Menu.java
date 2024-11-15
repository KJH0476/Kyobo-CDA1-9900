package kyobo.cda.SearchService.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Menu {

    private String menu_name;
    private double menu_price;
    private String image_url;
}
