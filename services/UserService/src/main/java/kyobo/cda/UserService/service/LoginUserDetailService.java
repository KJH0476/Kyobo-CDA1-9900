package kyobo.cda.UserService.service;

import kyobo.cda.UserService.entity.User;
import kyobo.cda.UserService.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class LoginUserDetailService implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * 주어진 이메일로 회원 정보를 조회하여 {@link LoginUserDetail} 객체를 반환한다.
     * 회원이 존재하지 않으면 {@link UsernameNotFoundException}을 던진다.
     *
     * @param email 인증 요청한 사용자의 이메일
     * @return {@link UserDetails} 객체로 반환된 회원의 인증 정보
     * @throws UsernameNotFoundException 주어진 이메일에 해당하는 회원이 존재하지 않을 때 발생
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

        // 회원이 존재하지 않으면 UsernameNotFoundException 발생
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException(email + " -> 사용자가 존재하지 않습니다."));

        return new LoginUserDetail(user, Collections.singletonList(() -> user.getRole().toString()));
    }
}
