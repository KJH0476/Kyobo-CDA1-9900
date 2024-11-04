package kyobo.cda.UserService.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UserUpdateRequestDto {

    @Size(max = 100)
    private String email;

    @Size(max = 20)
    private String username;
}
