package kyobo.cda.UserService.userTest;

import kyobo.cda.UserService.dto.UserDto;
import kyobo.cda.UserService.dto.UserUpdateRequestDto;
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

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserUpdateTest {

    @InjectMocks
    private UserService userService;

    @Mock
    private UserRepository userRepository;

    private User existingUser;

    @BeforeEach
    void setUp() {
        // 기존 사용자 설정
        existingUser = User.builder()
                .id(UUID.randomUUID())
                .email("test@example.com")
                .username("기존유저")
                .passwordHash("Password!1")
                .role(Role.USER)
                .build();
    }

    /**
     * 성공 케이스: 유효한 사용자 ID와 업데이트 요청 DTO를 제공하여 사용자의 이름을 성공적으로 업데이트하는 경우
     */
    @Test
    void 회원업데이트_성공_테스트() {
        // 업데이트할 사용자 ID와 요청 DTO 설정
        UUID userId = existingUser.getId();
        UserUpdateRequestDto updateRequest = new UserUpdateRequestDto(
                null, // 이메일 업데이트는 지원되지 않으므로 null로 설정
                "업데이트된유저명"
        );

        // userRepository.findById() 모킹 설정
        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));

        // userRepository.save() 모킹 설정
        User updatedUser = User.builder()
                .id(userId)
                .email(existingUser.getEmail())
                .username(updateRequest.getUsername())
                .passwordHash(existingUser.getPasswordHash())
                .role(existingUser.getRole())
                .build();

        when(userRepository.save(any(User.class))).thenReturn(updatedUser);

        // 업데이트 메서드 실행
        UserDto result = userService.updateUser(userId, updateRequest);

        // 결과 검증
        assertNotNull(result);
        assertEquals(updatedUser.getId(), result.getId());
        assertEquals(updatedUser.getEmail(), result.getEmail());
        assertEquals(updatedUser.getUsername(), result.getUsername());

        // 메서드 호출 여부 검증
        verify(userRepository, times(1)).findById(userId);
        verify(userRepository, times(1)).save(existingUser);
    }

    /**
     * 실패 케이스: 존재하지 않는 사용자 ID를 제공하여 IllegalArgumentException이 발생하는 경우
     */
    @Test
    void 회원업데이트_실패_존재하지않는사용자() {

        UUID nonExistentUserId = UUID.randomUUID();
        UserUpdateRequestDto updateRequest = new UserUpdateRequestDto(
                null,
                "업데이트된유저명"
        );

        // userRepository.findById() 모킹 설정: 사용자가 존재하지 않음
        when(userRepository.findById(nonExistentUserId)).thenReturn(Optional.empty());

        // 예외 발생 검증
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.updateUser(nonExistentUserId, updateRequest);
        });

        assertEquals("사용자를 찾을 수 없습니다.", exception.getMessage());

        // 메서드 호출 여부 검증
        verify(userRepository, times(1)).findById(nonExistentUserId);
        verify(userRepository, times(0)).save(any(User.class));
    }

    /**
     * 성공 케이스: 업데이트 요청 DTO에 업데이트할 필드가 없는 경우 (username이 null인 경우)
     * - 기존 사용자의 데이터가 변경되지 않음을 검증
     */
    @Test
    void 회원업데이트_성공_업데이트필드없음() {
        // 업데이트할 사용자 ID와 요청 DTO 설정 (업데이트할 필드 없음)
        UUID userId = existingUser.getId();
        UserUpdateRequestDto updateRequest = new UserUpdateRequestDto(
                null, // 이메일 업데이트는 지원되지 않으므로 null로 설정
                null  // username도 업데이트하지 않음
        );

        // userRepository.findById() 모킹 설정
        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));

        // userRepository.save() 모킹 설정 (변경 없음)
        when(userRepository.save(any(User.class))).thenReturn(existingUser);

        // 업데이트 메서드 실행
        UserDto result = userService.updateUser(userId, updateRequest);

        // 결과 검증
        assertNotNull(result);
        assertEquals(existingUser.getId(), result.getId());
        assertEquals(existingUser.getEmail(), result.getEmail());
        assertEquals(existingUser.getUsername(), result.getUsername());

        // 메서드 호출 여부 검증
        verify(userRepository, times(1)).findById(userId);
        verify(userRepository, times(1)).save(existingUser);
    }
}
