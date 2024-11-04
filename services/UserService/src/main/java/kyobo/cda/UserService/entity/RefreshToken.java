package kyobo.cda.UserService.entity;

import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.index.Indexed;

@Getter
@Builder
@RedisHash(value = "token", timeToLive = 2592000)   // TTL 30일
@NoArgsConstructor
@AllArgsConstructor
public class RefreshToken {

    @Id
    private Long id;
    private String refreshToken;

    @Indexed
    private String email;
}
