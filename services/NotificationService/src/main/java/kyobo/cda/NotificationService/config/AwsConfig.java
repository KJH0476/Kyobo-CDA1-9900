package kyobo.cda.NotificationService.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ses.SesClient;

@Configuration
public class AwsConfig {

    private final Region region = Region.AP_NORTHEAST_2;

    @Bean
    public SesClient sesClient() {
        return SesClient.builder()
                .region(region)
                .credentialsProvider(ProfileCredentialsProvider.create("ses-profile"))
                .build();
    }
}
