package kyobo.cda.ReservationService.repository;

import kyobo.cda.ReservationService.entity.RestaurantAvailability;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;
import java.util.UUID;

public interface RestaurantAvailabilityRepository extends JpaRepository<RestaurantAvailability, UUID> {

    Optional<RestaurantAvailability> findByRestaurantIdAndReservationDateAndReservationTime(UUID restaurantId, LocalDate reservationDate, LocalTime reservationTime);
}
