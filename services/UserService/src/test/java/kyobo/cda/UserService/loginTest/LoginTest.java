package kyobo.cda.UserService.loginTest;

import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import kyobo.cda.UserService.dto.LoginRequestDto;
import kyobo.cda.UserService.dto.LoginResponseDto;
import kyobo.cda.UserService.entity.RefreshToken;
import kyobo.cda.UserService.entity.Role;
import kyobo.cda.UserService.entity.User;
import kyobo.cda.UserService.repository.RefreshTokenRepository;
import kyobo.cda.UserService.repository.UserRepository;
import kyobo.cda.UserService.service.LoginService;
import kyobo.cda.UserService.service.LoginUserDetail;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import java.util.Base64;
import java.util.Collections;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@TestPropertySource(locations = "classpath:application-TEST.properties")
public class LoginTest {

    @InjectMocks
    private LoginService loginService;

    @Mock
    private AuthenticationManagerBuilder authManagerBuilder;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private Authentication authentication;

    private LoginRequestDto loginRequestDto;

    @BeforeEach
    void setUp() {
        loginRequestDto = new LoginRequestDto("test@example.com", "Password!1");
        String secretKey = Base64.getEncoder().encodeToString("asdofjqnojonvakqoieonvoqpjqpfpoqwefknlasknvlknlvkanvlanoi".getBytes());
        byte[] keyByte = Decoders.BASE64.decode(secretKey);
        Key signature = new SecretKeySpec(keyByte, SignatureAlgorithm.HS256.getJcaName());

        ReflectionTestUtils.setField(loginService, "secretKey", secretKey);
        ReflectionTestUtils.setField(loginService, "accessExpireTime", 1800000L);
        ReflectionTestUtils.setField(loginService, "refreshExpireTime", 2592000000L);
        ReflectionTestUtils.setField(loginService, "signature", signature);
    }

    @Test
    void 로그인_성공_테스트() throws Exception {
        // authManagerBuilder.getObject() 모킹
        when(authManagerBuilder.getObject()).thenReturn(authenticationManager);

        // authenticationManager.authenticate() 모킹
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);

        // 사용자 정보 설정
        User user = User.builder()
                .id(UUID.randomUUID())
                .email(loginRequestDto.getEmail())
                .username("테스트유저")
                .passwordHash("encodedPassword")
                .role(Role.USER)
                .build();

        // LoginUserDetail 객체 생성
        LoginUserDetail loginUserDetail = new LoginUserDetail(user,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));

        // authentication.getPrincipal() 모킹
        when(authentication.getPrincipal()).thenReturn(loginUserDetail);

        // authentication.getAuthorities() 모킹
        doReturn(Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")))
                .when(authentication).getAuthorities();

        // 저장 후 반환될 RefreshToken 객체 생성
        RefreshToken savedRefreshToken = RefreshToken.builder()
                .id(1L)
                .refreshToken("dummyRefreshToken")
                .email(loginRequestDto.getEmail())
                .build();

        // refreshTokenRepository.save() 모킹 설정
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenReturn(savedRefreshToken);

        // 로그인 메서드 실행
        LoginResponseDto responseDto = loginService.login(loginRequestDto);

        // 결과 검증
        assertNotNull(responseDto);
        assertEquals(HttpStatus.OK.value(), responseDto.getStatusCode());
        assertEquals("로그인 성공", responseDto.getMessage());
        assertNotNull(responseDto.getAccessToken());
        assertEquals(user.getEmail(), responseDto.getUserDto().getEmail());
        assertEquals(user.getUsername(), responseDto.getUserDto().getUsername());

        // 메서드 호출 여부 검증
        verify(authManagerBuilder, times(1)).getObject();
        verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(refreshTokenRepository, times(1)).save(any(RefreshToken.class));
    }

    @Test
    void 로그인_실패_인증실패_테스트() {
        // 인증 토큰 생성
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(loginRequestDto.getEmail(), loginRequestDto.getPassword());

        // authManagerBuilder.getObject() 모킹
        when(authManagerBuilder.getObject()).thenReturn(authenticationManager);

        // authenticationManager.authenticate() 모킹 - 인증 실패 시 예외 발생
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("인증 실패"));

        // 로그인 메서드 호출 및 예외 검증
        Exception exception = assertThrows(BadCredentialsException.class, () -> {
            loginService.login(loginRequestDto);
        });

        assertEquals("인증 실패", exception.getMessage());

        // 메서드 호출 여부 검증
        verify(authManagerBuilder, times(1)).getObject();
        verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(refreshTokenRepository, times(0)).save(any(RefreshToken.class));
    }

    @Test
    void 로그인_실패_사용자없음_테스트() {
        // 인증 토큰 생성
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(loginRequestDto.getEmail(), loginRequestDto.getPassword());

        // authManagerBuilder.getObject() 모킹
        when(authManagerBuilder.getObject()).thenReturn(authenticationManager);

        // authenticationManager.authenticate() 모킹 - 사용자 없음 예외 발생
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new UsernameNotFoundException("사용자를 찾을 수 없습니다."));

        // 로그인 메서드 호출 및 예외 검증
        Exception exception = assertThrows(UsernameNotFoundException.class, () -> {
            loginService.login(loginRequestDto);
        });

        assertEquals("사용자를 찾을 수 없습니다.", exception.getMessage());

        // 메서드 호출 여부 검증
        verify(authManagerBuilder, times(1)).getObject();
        verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(refreshTokenRepository, times(0)).save(any(RefreshToken.class));
    }
}
