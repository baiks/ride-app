package ride.sharing.com.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import ride.sharing.com.enums.RideStatus;
import ride.sharing.com.models.Ride;

import java.util.List;

public interface RideRepository extends JpaRepository<Ride, Long> {
    List<Ride> findByCustomerId(Long customerId);
    List<Ride> findByDriverId(Long driverId);
    List<Ride> findByStatus(RideStatus status);
}
