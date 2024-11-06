package kyobo.cda.ReservationService.repository;

import kyobo.cda.ReservationService.entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ReservationRepository extends JpaRepository<Reservation, UUID> {

    List<Reservation> findByUserEmail(String userEmail);
}
