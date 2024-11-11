package kyobo.cda.AuthorizationService.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .headers(headers -> headers
                        .frameOptions(ServerHttpSecurity.HeaderSpec.FrameOptionsSpec::disable) // frameOptions 비활성화
                )
                .authorizeExchange(exchanges -> exchanges
                        .pathMatchers("/**").permitAll()
                )
                .formLogin(ServerHttpSecurity.FormLoginSpec::disable); // formLogin 비활성화
        return http.build();
    }
}
