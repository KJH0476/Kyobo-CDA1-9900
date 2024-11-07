package kyobo.cda.AuthorizationService.filter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import kyobo.cda.AuthorizationService.dto.ReissuedResponse;
import kyobo.cda.AuthorizationService.entity.RefreshToken;
import kyobo.cda.AuthorizationService.service.JwtService;
import kyobo.cda.AuthorizationService.service.RefreshTokenService;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
public class AuthorizationFilter extends AbstractGatewayFilterFactory<AuthorizationFilter.Config> {

    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public AuthorizationFilter(JwtService jwtService, RefreshTokenService refreshTokenService) {
        super(Config.class);
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
    }

    /**
     * AuthFilter의 apply 메서드로, GatewayFilter를 생성하여 반환한다.
     * 이 필터는 JWT 토큰을 검증하고, 필요한 경우 액세스 토큰을 재발급하는 역할을 한다.
     *
     * <p>토큰 검증 흐름:</p>
     * <ul>
     *   <li>Authorization 헤더에서 Bearer 토큰을 추출한다.</li>
     *   <li>토큰이 없거나 Bearer로 시작하지 않으면 401 (Unauthorized) 응답을 반환한다.</li>
     *   <li>토큰이 만료되지 않았다면 토큰을 검증하고 권한을 확인한 뒤 요청을 처리한다.</li>
     *   <li>토큰이 만료되었을 경우, Refresh 토큰을 사용하여 새로운 액세스 토큰을 발급한다.</li>
     *   <li>Refresh 토큰이 유효하지 않으면 401 (Unauthorized) 응답을 반환한다.</li>
     *   <li>Refresh 토큰이 유효하면 새로운 액세스 토큰과 갱신된 Refresh 토큰을 Redis에 저장하고 반환한다.</li>
     *   <li>권한이 없는 경우 403 (Forbidden) 응답을 반환한다.</li>
     * </ul>
     *
     * <p>예외 처리:</p>
     * <ul>
     *   <li>ExpiredJwtException: 액세스 토큰이 만료되었을 때 처리</li>
     *   <li>IllegalArgumentException: 리프레시 토큰과 관련된 오류 발생 시 처리</li>
     *   <li>기타 Exception: 일반적인 토큰 관련 오류 처리</li>
     * </ul>
     *
     * @param config 필터의 설정 값이 담긴 AuthFilter.Config 객체
     * @return GatewayFilter JWT 인증을 처리하는 GatewayFilter 객체
     * @throws ExpiredJwtException 액세스 토큰이 만료되었을 때 발생
     * @throws IllegalArgumentException 리프레시 토큰이 잘못되었을 때 발생
     * @throws Exception 일반적인 토큰 관련 오류 발생 시 처리
     */
    @Override
    public GatewayFilter apply(AuthorizationFilter.Config config) {
        return ((exchange, chain) -> {

            String path = exchange.getRequest().getURI().getPath();
            MDC.put("requestUri", path);

            if (path.equals("/health")) {
                exchange.getResponse().setStatusCode(HttpStatus.OK);
                return exchange.getResponse().writeWith(
                        Mono.just(exchange.getResponse()
                                .bufferFactory()
                                .wrap("OK".getBytes()))
                );
            }

            String authorization = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

            // 토큰 값이 없거나 Bearer로 시작하지 않으면 null 반환
            if(authorization==null || !authorization.startsWith("Bearer ")){
                // 응답 상태 코드 401 (Unauthorized)로 설정 및 응답 완료
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            }

            try {
                String token = authorization.substring(7);
                Claims claims = jwtService.getClaims(token);

                // 토큰 검증 로직 추가
                if (jwtService.validateToken(token)) log.info("액세스 토큰 검증 성공");

                // 이메일 정보 추출 후 다음 요청 헤더에 저장
                String requestEmail = claims.getSubject();
                // UUID 로 requestId 생성 및 다음 요청 헤더에 저장
                String requestId = UUID.randomUUID().toString().substring(0, 8);
                // 요청 헤더에 email, requestId 추가
                ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
                        .header("X-User-Email", requestEmail)
                        .header("X-Request-Id", requestId)
                        .build();

                // MDC에 로그 정보 저장
                MDC.put("email", requestEmail);
                MDC.put("requestId", requestId);

                exchange = exchange.mutate().request(mutatedRequest).build();

                // 권한 체크
                String role = claims.get("auth", String.class);
                if (role == null || !config.getAllowedRoles().contains(role)) {
                    return forbiddenResponse(exchange, "access denied");
                }
            } catch(ExpiredJwtException ej) {
                try {
                    log.info("액세스 토큰 만료");
                    String email = ej.getClaims().getSubject();
                    String refreshToken = refreshTokenService.findRefreshToken(email).getRefreshToken();

                    // 갱신 토큰 검증
                    if (jwtService.validateToken(refreshToken)) {
                        log.info("리프레시 토큰 검증 성공");

                        // 갱신 토큰으로 새로운 액세스 토큰 발급
                        String newAccessToken = jwtService.createToken(email, ej.getClaims().get("auth", String.class), jwtService.getAccessExpireTime());
                        String newRefreshToken = jwtService.createToken(email, ej.getClaims().get("auth", String.class), jwtService.getRefreshExpireTime());

                        // 기존 갱신 토큰 삭제
                        refreshTokenService.removeRefreshToken(email);

                        // Redis에 갱신 토큰 저장
                        refreshTokenService.saveRefreshToken(RefreshToken.builder()
                                .refreshToken(newRefreshToken)
                                .email(email)
                                .build());

                        return okResponse(exchange, "success reissued token", newAccessToken);
                    } else {
                        log.info("리프레시 토큰 검증 실패");
                        return unauthorizedResponse(exchange, "invalid refresh token");
                    }
                } catch (IllegalArgumentException e) {
                    log.error("error : {}", e.getMessage());
                    return unauthorizedResponse(exchange, "expired refresh token");
                }
            } catch (Exception e) {
                log.error("error : {}", e.getMessage());
                return unauthorizedResponse(exchange, "invalid token");
            }

            return chain.filter(exchange);
        });
    }

    // 200 OK 응답
    private Mono<Void> okResponse(ServerWebExchange exchange, String message, String accessToken) {
        return sendResponse(exchange, HttpStatus.OK, message, accessToken);
    }

    // 401 Unauthorized 응답
    private Mono<Void> unauthorizedResponse(ServerWebExchange exchange, String message) {
        return sendResponse(exchange, HttpStatus.UNAUTHORIZED, message);
    }

    // 403 Forbidden 응답
    private Mono<Void> forbiddenResponse(ServerWebExchange exchange, String message) {
        return sendResponse(exchange, HttpStatus.FORBIDDEN, message);
    }

    // 성공 응답 처리
    private Mono<Void> sendResponse(ServerWebExchange exchange, HttpStatus status, String message, String accessToekn) {
        exchange.getResponse().setStatusCode(status);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);

        ReissuedResponse successResponse = ReissuedResponse.builder()
                .statusCode(status.value())
                .message(message)
                .accessToken(accessToekn)
                .build();

        return writeResponse(exchange, successResponse);
    }

    // 공통 에러 응답 처리
    private Mono<Void> sendResponse(ServerWebExchange exchange, HttpStatus status, String message) {
        exchange.getResponse().setStatusCode(status);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);

        ReissuedResponse errorResponse = ReissuedResponse.builder()
                .statusCode(status.value())
                .message(message)
                .build();

        return writeResponse(exchange, errorResponse);
    }

    // JSON 변환 및 응답 작성 로직을 공통 메서드로 분리
    private Mono<Void> writeResponse(ServerWebExchange exchange, ReissuedResponse errorResponse) {
        try {
            // 객체를 JSON 형식으로 변환
            String responseBody = objectMapper.writeValueAsString(errorResponse);
            byte[] bytes = responseBody.getBytes(StandardCharsets.UTF_8);

            return exchange.getResponse().writeWith(Mono.just(exchange.getResponse()
                    .bufferFactory().wrap(bytes)));
        } catch (JsonProcessingException e) {
            exchange.getResponse().setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
            return exchange.getResponse().setComplete();
        }
    }

    // 필터 설정 값
    // allowedRoles: 허용된 권한 목록 정의
    @Data
    public static class Config {
        private List<String> allowedRoles;
    }
}
