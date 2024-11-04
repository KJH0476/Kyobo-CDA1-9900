package kyobo.cda.AuthorizationService.service;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.SignatureException;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import java.util.Date;

@Slf4j
@Getter
@Service
@RequiredArgsConstructor
public class JwtService {

    @Value("${jwt.secret-key}")
    private String secretKey;

    @Value("${jwt.access-expire-time}")
    private long accessExpireTime;

    @Value("${jwt.refresh-expire-time}")
    private long refreshExpireTime;

    private Key signature;
    private final SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;

    /**
     * 주어진 JWT 토큰을 검증하는 메서드.
     * 토큰의 서명 및 형식이 올바른지 확인하며, 여러 가지 JWT 관련 예외 상황을 처리한다.
     *
     * @param token 검증할 JWT 토큰
     * @return 토큰이 유효하면 true, 그렇지 않으면 false
     *
     * @throws ExpiredJwtException 토큰이 만료되었을 때 발생
     * @throws UnsupportedJwtException 지원되지 않는 JWT 형식일 때 발생
     * @throws MalformedJwtException JWT가 올바르게 구성되지 않았을 때 발생
     * @throws SignatureException 서명이 올바르지 않을 때 발생
     * @throws JwtException 기타 JWT 관련 오류 발생
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(signature).build().parseClaimsJws(token);
        } catch (ExpiredJwtException e) {
            log.error("JWT 토큰이 만료됨: {}", e.getMessage());
            return false;
        } catch (UnsupportedJwtException e) {
            log.error("JWT 형식이 잘못됨 {}", e.getMessage());
            return false;
        } catch (MalformedJwtException e) {
            log.error("JWT가 올바르게 구성되지 않음: {}", e.getMessage());
            return false;
        } catch (SignatureException e) {
            log.error("JWT 서명이 잘못됨: {}", e.getMessage());
            return false;
        } catch (JwtException e) {
            log.error("JWT 토큰이 잘못됨: {}", e.getMessage());
            return false;
        }
        return true;
    }

    /**
     * 주어진 이메일과 권한 정보를 기반으로 JWT 토큰을 생성하는 메서드이다.
     * 토큰에는 주체(subject)로 이메일이, 클레임(claim)으로 권한 정보가 포함된다.
     *
     * @param email 토큰에 포함될 사용자 이메일
     * @param authorities 토큰에 포함될 사용자 권한 정보
     * @param expireTime 토큰의 만료 시간 (밀리초 단위)
     * @return 생성된 JWT 토큰 문자열 반환
     */
    public String createToken(String email, String authorities, long expireTime){
        return Jwts.builder()
                .setSubject(email)
                .claim("auth", authorities)
                .signWith(signature, signatureAlgorithm)
                .setExpiration(new Date(System.currentTimeMillis() + expireTime))
                .compact();
    }

    public Claims getClaims(String token) {
        return Jwts.parserBuilder().setSigningKey(signature).build().parseClaimsJws(token).getBody();
    }

    /**
     * 애플리케이션 초기화 시 JWT 서명을 생성하는 메서드이다.
     * Base64로 인코딩된 비밀 키를 디코딩하여 서명(Signature)을 생성한다.
     * 이 서명은 JWT의 서명 및 검증에 사용된다.
     *
     * @throws Exception 서명 생성 중 문제가 발생할 경우 발생
     */
    @PostConstruct
    public void initSignature() throws Exception {
        log.info("JWT 서명 생성");

        // Base64로 인코딩된 비밀 키를 디코딩하여 바이트 배열로 저장
        byte[] keyByte = Decoders.BASE64.decode(secretKey);

        //디코딩된 비밀키와 서명 알고리즘을 넣어 Key(=서명) 생성
        //SecretKeySpec : Jwt를 서명하거나 서명을 검증하는 데 사용
        signature = new SecretKeySpec(keyByte, SignatureAlgorithm.HS256.getJcaName());
    }
}
