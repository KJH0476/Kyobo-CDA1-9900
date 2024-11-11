package kyobo.cda.UserService.service;

import kyobo.cda.UserService.dto.UserDto;
import kyobo.cda.UserService.entity.User;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

@Getter
public class LoginUserDetail extends org.springframework.security.core.userdetails.User {

    private final UserDto userDto;

    /**
     * 주어진 {@link User} 엔티티와 권한 정보로 {@link LoginUserDetail} 객체를 생성한다.
     *
     * @param user 로그인한 회원의 정보를 담은 엔티티 객체
     * @param authorities 회원의 권한 목록
     */
    public LoginUserDetail(User user, Collection<? extends GrantedAuthority> authorities) {
        super(user.getEmail(), user.getPasswordHash(), authorities);
        this.userDto = UserDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .username(user.getUsername())
                .build();
    }
}
