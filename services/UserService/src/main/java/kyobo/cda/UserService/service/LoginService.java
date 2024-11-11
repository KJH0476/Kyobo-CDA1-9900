package kyobo.cda.UserService.service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import jakarta.annotation.PostConstruct;
import kyobo.cda.UserService.dto.LoginRequestDto;
import kyobo.cda.UserService.dto.LoginResponseDto;
import kyobo.cda.UserService.entity.RefreshToken;
import kyobo.cda.UserService.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import java.util.Date;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class LoginService {

    @Value("${jwt.secret-key}")
    private String secretKey;

    @Value("${jwt.access-expire-time}")
    private long accessExpireTime;

    @Value("${jwt.refresh-expire-time}")
    private long refreshExpireTime;

    private Key signature;
    private final SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;
    private final AuthenticationManagerBuilder authManagerBuilder;
    private final RefreshTokenRepository refreshTokenRepository;

    /**
     * 사용자의 로그인 요청을 처리하는 메서드이다.
     * 입력된 이메일과 비밀번호로 인증을 수행하고, 성공 시 액세스 토큰과 리프레시 토큰을 생성하여 반환한다.
     * 생성된 리프레시 토큰은 Redis에 저장된다.
     *
     * <p>로그인 절차:</p>
     * <ul>
     *     <li>이메일과 비밀번호를 이용하여 인증 토큰을 생성</li>
     *     <li>인증이 성공하면 SecurityContextHolder에 인증 정보 저장</li>
     *     <li>사용자의 권한 정보를 기반으로 JWT 액세스 토큰 및 리프레시 토큰 생성</li>
     *     <li>리프레시 토큰은 Redis에 저장</li>
     * </ul>
     *
     * @param dto 로그인 요청 정보를 담은 DTO
     * @return 로그인 응답 DTO (상태 코드, 메시지, 액세스 토큰, 사용자 정보 포함)
     * @throws Exception 인증 실패 또는 기타 예외 발생 시 발생
     */
    public LoginResponseDto login(LoginRequestDto dto) throws Exception {

        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(dto.getEmail(), dto.getPassword());
        // CustomUserDetailService 의 loadUser() 실행됨
        Authentication authentication = authManagerBuilder.getObject().authenticate(authenticationToken);

        // 사용자가 로그인한 직후의 인증 상태를 즉시 반영
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // 로그인한 사용자의 정보를 가져옴
        // UserDetails 객체를 LoginUserDetail로 캐스팅
        LoginUserDetail loginUserDetail = (LoginUserDetail) authentication.getPrincipal();

        // 사용자의 권한 정보를 콤마(,)로 구분하여 authorities에 저장
        String authorities = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        // 액세스 토큰과 리프레시 토큰 생성
        String accessToken = createToken(dto.getEmail(), authorities, accessExpireTime);
        String refreshToken = createToken(dto.getEmail(), authorities, refreshExpireTime);

        // Redis에 refreshToken 저장
        saveRefreshToken(RefreshToken.builder()
                .refreshToken(refreshToken)
                .email(dto.getEmail())
                .build());

        // 로그인 성공 시 응답 DTO 반환
        return LoginResponseDto.builder()
                .statusCode(HttpStatus.OK.value())
                .message("로그인 성공")
                .accessToken(accessToken)
                .userDto(loginUserDetail.getUserDto())
                .build();
    }

    // JWT 토큰 생성
    public String createToken(String userId, String authorities, long expireTime){
        return Jwts.builder()
                .setSubject(userId)
                .claim("auth", authorities)
                .signWith(signature, signatureAlgorithm)
                .setExpiration(new Date(System.currentTimeMillis() + expireTime))
                .compact();
    }

    //갱신 토큰 저장
    public void saveRefreshToken(RefreshToken refreshToken){
        refreshTokenRepository.save(RefreshToken.builder()
                .refreshToken(refreshToken.getRefreshToken())
                .email(refreshToken.getEmail()).build());
    }

    @PostConstruct
    public void initSignature() throws Exception {
        byte[] keyByte = Decoders.BASE64.decode(secretKey);
        signature = new SecretKeySpec(keyByte, SignatureAlgorithm.HS256.getJcaName());
    }
}
