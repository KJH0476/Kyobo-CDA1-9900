package kyobo.cda.AuthorizationService.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.index.Indexed;

@Getter
@RedisHash(value = "token", timeToLive = 2592000)   // TTL 30일
@NoArgsConstructor
@AllArgsConstructor
public class RefreshToken {

    @Id
    private Long id;
    private String refreshToken;

    @Indexed
    private String email;   // 이메일 값으로 리프레시 토큰 조회

    @Builder
    public RefreshToken(String refreshToken, String email) {
        this.refreshToken = refreshToken;
        this.email = email;
    }
}
