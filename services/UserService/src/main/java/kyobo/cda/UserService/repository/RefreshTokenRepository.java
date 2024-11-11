package kyobo.cda.UserService.repository;

import kyobo.cda.UserService.entity.RefreshToken;
import org.springframework.data.repository.CrudRepository;

public interface RefreshTokenRepository extends CrudRepository<RefreshToken, String> {
}
