package kyobo.cda.SearchService.config;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import org.apache.http.HttpHost;
import org.opensearch.client.RestClient;
import org.opensearch.client.RestClientBuilder;
import org.opensearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AwsConfig {

    @Value("${opensearch.host}")
    private String host;
    @Value("${opensearch.region}")
    private String region;
    private static final String serviceName = "es";

    @Bean
    public RestHighLevelClient restHighLevelClient() {
        AWSCredentialsProvider credentialsProvider = new DefaultAWSCredentialsProviderChain();

        // AWSRequestSigningApacheInterceptor 생성
        AWSRequestSigningApacheInterceptor interceptor = new AWSRequestSigningApacheInterceptor(
                serviceName,
                region,
                credentialsProvider
        );

        RestClientBuilder restClientBuilder = RestClient.builder(new HttpHost(host, 443, "https"))
                .setHttpClientConfigCallback(httpClientBuilder -> httpClientBuilder.addInterceptorLast(interceptor));

        return new RestHighLevelClient(restClientBuilder);
    }
}
