package kyobo.cda.ReservationService.repository;

import kyobo.cda.ReservationService.entity.WaitList;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface WaitListRepository extends JpaRepository<WaitList, UUID> {
}
