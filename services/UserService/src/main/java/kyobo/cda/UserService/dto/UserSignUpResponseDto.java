package kyobo.cda.UserService.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserSignUpResponseDto {

    private int statusCode;
    private String message;
    private UserDto userDto;
}
