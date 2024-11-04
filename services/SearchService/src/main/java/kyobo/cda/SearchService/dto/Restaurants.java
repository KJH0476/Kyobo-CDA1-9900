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

    private UUID restaurantId;
    private String restaurantName;
    private String address;
    private List<String> category;
    private String phoneNumber;
    private String description;
    private Timestamp createdAt;
    private Timestamp updatedAt;
}
