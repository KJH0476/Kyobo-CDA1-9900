package kyobo.cda.AuthorizationService.service;

import kyobo.cda.AuthorizationService.entity.RefreshToken;
import kyobo.cda.AuthorizationService.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    // 갱신 토큰 조회
    public RefreshToken findRefreshToken(String email) {
        return refreshTokenRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("not found refresh token"));
    }

    // 갱신 토큰 저장
    public void saveRefreshToken(RefreshToken token){
        refreshTokenRepository.save(RefreshToken.builder()
                .refreshToken(token.getRefreshToken())
                .email(token.getEmail()).build());
    }

    // 갱신 토큰 삭제
    public void removeRefreshToken(String email){
        refreshTokenRepository.findByEmail(email)
                .ifPresent(refreshTokenRepository::delete);
    }
}
