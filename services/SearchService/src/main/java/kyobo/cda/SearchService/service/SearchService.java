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

    public SearchResult searchRestaurant(String title, List<String> categories, String address, Object[] searchAfter) throws IOException {
        log.info("식당 검색");

        SearchRequest searchRequest = new SearchRequest(index);
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();

        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();

        // 제목 필터링
        if (title != null && !title.isEmpty()) {
            boolQuery.must(QueryBuilders.matchQuery("title", title));
        }

        // 스킬 필터링
        if (categories != null && !categories.isEmpty()) {
            boolQuery.filter(QueryBuilders.termsQuery("categories", categories));
        }

        // 경력 유형 필터링
        if (address != null && !address.isEmpty()) {
            boolQuery.filter(QueryBuilders.matchQuery("address", address));
        }

        sourceBuilder.sort("_id", SortOrder.ASC);
        sourceBuilder.sort("updatedAt", SortOrder.DESC);

        // search_after 설정
        if (searchAfter != null && searchAfter.length > 0) {
            log.info("search after");
            sourceBuilder.searchAfter(searchAfter);
        }
        sourceBuilder.size(20);
        sourceBuilder.query(boolQuery);
        searchRequest.source(sourceBuilder);

        // 검색 요청 후 응답 저장
        SearchResponse searchResponse = restHighLevelClient.search(
                searchRequest,
                RequestOptions.DEFAULT.toBuilder()
                        .addHeader("Content-Type", ContentType.APPLICATION_JSON.getMimeType())
                        .build());

        List<Restaurants> restaurantResults = new ArrayList<>();
        SearchHit[] hits = searchResponse.getHits().getHits();
        for(SearchHit hit : hits){
            try {
                Restaurants restaurant = objectMapper.readValue(hit.getSourceAsString(), Restaurants.class);
                restaurantResults.add(restaurant);
            } catch (IOException e) {
                log.info("Failed to parse search response", e);
            }
        }

        // 검색 결과에서 마지막 문서 sort 필드 추출
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