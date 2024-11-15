package kyobo.cda.SearchService.controller;

import kyobo.cda.SearchService.dto.SearchResponseDto;
import kyobo.cda.SearchService.dto.SearchResult;
import kyobo.cda.SearchService.service.SearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
public class SearchController {

    private final SearchService searchService;

    @GetMapping("/restaurants")
    public ResponseEntity<SearchResponseDto> search(
            @RequestParam(value = "restaurant_name", required = false) String restaurantName,
            @RequestParam(value = "food_type", required = false) List<String> foodType,
            @RequestParam(value = "address", required = false) String address,
            @RequestParam(value = "searchAfter", required = false) Object[] searchAfter
    ) {

        try {
            SearchResult searchResult = searchService.searchRestaurant(restaurantName, foodType, address, searchAfter);

            return new ResponseEntity<>(SearchResponseDto.builder()
                    .statusCode(HttpStatus.OK.value())
                    .message("식당 검색 성공")
                    .restaurants(searchResult.getRestaurants())
                    .searchAfter(searchResult.getSearchAfter())
                    .build(), HttpStatus.OK);
        } catch (Exception e) {
            log.error("식당 검색 실패", e);

            return new ResponseEntity<>(SearchResponseDto.builder()
                    .statusCode(HttpStatus.BAD_REQUEST.value())
                    .message("식당 검색 실패")
                    .build(), HttpStatus.BAD_REQUEST);
        }
    }
}
