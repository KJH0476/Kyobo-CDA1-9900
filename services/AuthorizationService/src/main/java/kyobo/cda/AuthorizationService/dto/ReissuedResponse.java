package kyobo.cda.AuthorizationService.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReissuedResponse {

    private int statusCode; // http 응답 상태 코드
    private String message; // 응답 메시지
    private String accessToken; // 갱신된 액세스 토큰
}
