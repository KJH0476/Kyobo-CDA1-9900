package kyobo.cda.SearchService.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import kyobo.cda.SearchService.dto.Restaurants;
import kyobo.cda.SearchService.dto.SearchResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.entity.ContentType;
import org.opensearch.action.search.SearchRequest;
import org.opensearch.action.search.SearchResponse;
import org.opensearch.client.RequestOptions;
import org.opensearch.client.RestHighLevelClient;
import org.opensearch.index.query.BoolQueryBuilder;
import org.opensearch.index.query.QueryBuilders;
import org.opensearch.search.SearchHit;
import org.opensearch.search.builder.SearchSourceBuilder;
import org.opensearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SearchService {

    @Value("${opensearch.index}")
    private String index;
    private final RestHighLevelClient restHighLevelClient;
    private final ObjectMapper objectMapper;

    /**
     * 식당 검색 서비스
     * 식당명, 음식 종류, 주소를 검색 조건으로 받아 검색한다.
     *
     * @param restaurant_name 식당명
     * @param food_type 음식 유형
     * @param address 주소
     * @param searchAfter OpenSearch SearchAfter 파라미터
     * @return SearchResult 식당 검색 결과 반환
     * @throws IOException OpenSearch API 호출 중 예외 발생 시
     */
    public SearchResult searchRestaurant(String restaurant_name, List<String> food_type, String address, Object[] searchAfter) throws IOException {
        log.info("식당 검색");

        // OpenSearch 인덱스에 대한 검색 요청 객체 생성
        SearchRequest searchRequest = new SearchRequest(index);
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();

        // Bool 쿼리 빌더를 사용하여 여러 조건을 결합
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();

        // 식당명이 제공되었을 경우, 'restaurant_name' 필드에 대해 매치 쿼리 추가
        if (restaurant_name != null && !restaurant_name.isEmpty()) {
            boolQuery.must(QueryBuilders.matchQuery("restaurant_name", restaurant_name));
        }

        // 음식 종류가 제공되었을 경우, 'food_type' 필드에 대해 terms 필터 추가
        if (food_type != null && !food_type.isEmpty()) {
            boolQuery.filter(QueryBuilders.termsQuery("food_type", food_type));
        }

        // 주소가 제공되었을 경우, 'address' 필드에 대해 매치 필터 추가
        if (address != null && !address.isEmpty()) {
            boolQuery.filter(QueryBuilders.matchQuery("address", address));
        }

        // 정렬 조건 설정: _id를 오름차순, update_at을 내림차순으로 정렬
        sourceBuilder.sort("_id", SortOrder.ASC);
        sourceBuilder.sort("update_at", SortOrder.DESC);

        // search_after 파라미터가 제공되었을 경우 설정 (페이지네이션을 위한 설정)
        if (searchAfter != null && searchAfter.length > 0) {
            log.info("search after");
            sourceBuilder.searchAfter(searchAfter);
        }

        sourceBuilder.size(10);

        // 검색 요청 객체 설정
        sourceBuilder.query(boolQuery);
        searchRequest.source(sourceBuilder);

        // 검색 요청 후 응답 저장
        SearchResponse searchResponse = restHighLevelClient.search(
                searchRequest,
                RequestOptions.DEFAULT.toBuilder()
                        .addHeader("Content-Type", ContentType.APPLICATION_JSON.getMimeType())
                        .build());

        log.info("searchResponse: {}", searchResponse.toString());

        // 검색 결과 리스트에 저장
        List<Restaurants> restaurantResults = new ArrayList<>();
        SearchHit[] hits = searchResponse.getHits().getHits();
        for(SearchHit hit : hits){
            try {
                Restaurants restaurant = objectMapper.readValue(hit.getSourceAsString(), Restaurants.class);
                restaurantResults.add(restaurant);
            } catch (IOException e) {
                log.error("Failed to parse search response", e);
            }
        }

        // 검색 결과 중 마지막 문서의 sort 값을 추출하여 다음 페이지를 위한 search_after 설정
        Object[] sortValues = null;
        if(hits.length>0){
            SearchHit lastHit = hits[hits.length-1];
            sortValues = lastHit.getSortValues();
        }

        return SearchResult.builder()
                .restaurants(restaurantResults)
                .searchAfter(sortValues)
                .build();
    }
}
