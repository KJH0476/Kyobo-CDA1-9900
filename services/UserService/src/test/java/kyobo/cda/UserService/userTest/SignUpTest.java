package kyobo.cda.UserService.userTest;

import kyobo.cda.UserService.dto.UserDto;
import kyobo.cda.UserService.dto.UserSignUpRequestDto;
import kyobo.cda.UserService.entity.Role;
import kyobo.cda.UserService.entity.User;
import kyobo.cda.UserService.repository.UserRepository;
import kyobo.cda.UserService.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SignUpTest {

    @InjectMocks
    private UserService userService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private UserSignUpRequestDto userSignUpRequestDto;

    @BeforeEach
    void setUp() {
        // 테스트용 회원가입 요청 DTO 생성
        userSignUpRequestDto = new UserSignUpRequestDto(
                "test@example.com",
                "테스트유저",
                "Password!1"
        );
    }

    @Test
    void 회원가입_성공_테스트() {
        // 이미 존재하는 이메일이 없음을 설정
        when(userRepository.findByEmail(userSignUpRequestDto.getEmail()))
                .thenReturn(Optional.empty());

        // 패스워드 인코딩 설정
        when(passwordEncoder.encode(userSignUpRequestDto.getPassword()))
                .thenReturn("encodedPassword");

        // 저장 후 반환될 User 객체 생성 (ID 포함)
        User savedUser = User.builder()
                .id(UUID.randomUUID())
                .email(userSignUpRequestDto.getEmail())
                .username(userSignUpRequestDto.getUsername())
                .passwordHash("encodedPassword")
                .role(Role.USER)
                .build();

        // userRepository.save() 모킹 설정
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        // 회원가입 메서드 실행
        UserDto result = userService.signupUser(userSignUpRequestDto);

        // 결과 검증
        assertNotNull(result);
        UUID.fromString(result.getId().toString()); // UUID 형식 검증 (예외 발생 시 테스트 실패)
        assertEquals(savedUser.getEmail(), result.getEmail());
        assertEquals(savedUser.getUsername(), result.getUsername());

        // 메서드 호출 여부 검증
        // times(1)은 해당 메서드가 1회 호출되었음을 의미
        // 모든 메서드는 1회 호출되어야 함
        verify(userRepository, times(1)).findByEmail(userSignUpRequestDto.getEmail());
        verify(passwordEncoder, times(1)).encode(userSignUpRequestDto.getPassword());
        verify(userRepository, times(1)).save(any(User.class));
    }


    @Test
    void 회원가입_실패_이미존재하는이메일() {
        // 이미 존재하는 이메일 설정
        User existingUser = User.builder()
                .id(UUID.randomUUID())
                .email(userSignUpRequestDto.getEmail())
                .username("기존유저")
                .passwordHash("encodedPassword")
                .role(Role.USER)
                .build();

        when(userRepository.findByEmail(userSignUpRequestDto.getEmail()))
                .thenReturn(Optional.of(existingUser));

        // 예외 발생 검증
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.signupUser(userSignUpRequestDto);
        });

        assertEquals("user already exists", exception.getMessage());

        // 메서드 호출 여부 검증
        // times(1)은 해당 메서드가 1회 호출되었음을 의미
        // 이미 사용자가 존재하므로 findByEmail() 메서드에서 예외가 발생해야하고 encode()와 save() 메서드는 호출되지 않아야 함
        verify(userRepository, times(1)).findByEmail(userSignUpRequestDto.getEmail());
        verify(passwordEncoder, times(0)).encode(anyString());
        verify(userRepository, times(0)).save(any(User.class));
    }
}
