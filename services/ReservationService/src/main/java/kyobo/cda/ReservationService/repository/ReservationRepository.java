package kyobo.cda.ReservationService.repository;

import kyobo.cda.ReservationService.entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ReservationRepository extends JpaRepository<Reservation, UUID> {

    List<Reservation> findByUserEmail(String userEmail);
    Optional<Reservation> findByUserEmailAndRestaurantIdAndReservationDateTime(String userEmail, UUID restaurantId, LocalDateTime reservationDateTime);
}
