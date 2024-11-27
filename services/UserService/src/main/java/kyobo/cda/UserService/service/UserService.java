package kyobo.cda.UserService.service;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import jakarta.annotation.PostConstruct;
import kyobo.cda.UserService.dto.UserDto;
import kyobo.cda.UserService.dto.UserSignUpRequestDto;
import kyobo.cda.UserService.dto.UserUpdateRequestDto;
import kyobo.cda.UserService.entity.Role;
import kyobo.cda.UserService.entity.User;
import kyobo.cda.UserService.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    @Value("${jwt.signup.secret-key}")
    private String signUpSecretKey;
    private Key signature;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * 사용자의 회원가입 요청을 처리하는 메서드이다.
     * 입력된 회원 정보를 저장하고 저장된 회원 정보를 반환한다.
     *
     * @param userSignUpRequestDto 회원가입 요청 정보를 담은 DTO
     * @return UserDto 새로 회원가입한 회원 정보 반환
     * @throws IllegalArgumentException 이미 가입된 회원인 경우 예외 발생
     */
    public UserDto signupUser(UserSignUpRequestDto userSignUpRequestDto) {

        // 이미 가입된 회원인지 확인
        // 이미 가입된 회원이면 예외 발생 -> controller에서 예외 처리
        userRepository.findByEmail(userSignUpRequestDto.getEmail())
                .ifPresent(user -> {
                    throw new IllegalArgumentException("user already exists");
                });

        // 회원 정보 생성 및 저장
        User createdUser = userRepository.save(User.builder()
                .email(userSignUpRequestDto.getEmail())
                .username(userSignUpRequestDto.getUsername())
                .passwordHash(passwordEncoder.encode(userSignUpRequestDto.getPassword()))
                .role(Role.USER)
                .build());

        return UserDto.builder()
                .id(createdUser.getId())
                .email(createdUser.getEmail())
                .username(createdUser.getUsername())
                .build();
    }

    public boolean validateSignUpToken(String token) {
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
     * 사용자 정보를 업데이트하는 메서드이다.
     * 입력된 사용자 ID와 업데이트 정보를 이용하여 사용자 정보를 업데이트하고 업데이트된 사용자 정보를 반환한다.
     *
     * @param id
     * @param request
     * @return UserDto 업데이트된 사용자 정보 반환
     */
    @Transactional
    public UserDto updateUser(UUID id, UserUpdateRequestDto request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // 사용자 정보 업데이트
        if (request.getUsername() != null) {
            user.setUsername(request.getUsername());
        }

        userRepository.save(user);

        // 응답 DTO 생성
        return UserDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .username(user.getUsername())
                .build();
    }

    public UserDto getUserById(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        return UserDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .username(user.getUsername())
                .build();
    }

    @PostConstruct
    public void initSignature() throws Exception {
        byte[] keyByte = Decoders.BASE64.decode(signUpSecretKey);
        signature = new SecretKeySpec(keyByte, SignatureAlgorithm.HS256.getJcaName());
    }
}
