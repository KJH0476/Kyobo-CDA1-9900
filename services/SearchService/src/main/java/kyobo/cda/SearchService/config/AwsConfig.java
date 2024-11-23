package kyobo.cda.SearchService.config;

import com.amazonaws.auth.AWS4Signer;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import org.apache.http.HttpHost;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
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

    private static final String serviceName = "es"; // Amazon OpenSearch Service는 여전히 "es"를 사용

    @Bean
    public RestHighLevelClient restHighLevelClient() {
        // AWS 자격 증명 제공자 설정
        AWSCredentialsProvider credentialsProvider = new DefaultAWSCredentialsProviderChain();

        // AWS4 서명자 설정
        AWS4Signer signer = new AWS4Signer();
        signer.setServiceName(serviceName);
        signer.setRegionName(region);

        // AWSRequestSigningApacheInterceptor 생성
        AWSRequestSigningApacheInterceptor interceptor = new AWSRequestSigningApacheInterceptor(
                serviceName,
                signer,
                credentialsProvider
        );

        // RestClientBuilder에 인터셉터 추가
        RestClientBuilder restClientBuilder = RestClient.builder(new HttpHost(host, 443, "https"))
                .setHttpClientConfigCallback(new RestClientBuilder.HttpClientConfigCallback() {
                    @Override
                    public HttpAsyncClientBuilder customizeHttpClient(HttpAsyncClientBuilder httpClientBuilder) {
                        return httpClientBuilder.addInterceptorLast(interceptor);
                    }
                });

        return new RestHighLevelClient(restClientBuilder);
    }
}