package kyobo.cda.SearchService.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Restaurants {

    private String id;
    private String restaurant_name;
    private String address;
    private List<Menu> menu;
    private List<String> food_type;
    private String phone_number;
    private boolean availability;
    private double longitude;
    private double latitude;
    private long update_at;
}
