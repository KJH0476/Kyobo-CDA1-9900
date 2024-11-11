package kyobo.cda.UserService.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Role {

    ADMIN("ROLE_ADMIN", "ADMIN"),
    USER("ROLE_USER", "USER");

    private final String key;
    private final String value;
}
