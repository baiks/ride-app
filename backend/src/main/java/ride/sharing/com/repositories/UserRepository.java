package ride.sharing.com.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ride.sharing.com.enums.DriverStatus;
import ride.sharing.com.enums.Role;
import ride.sharing.com.models.User;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    List<User> findByRoleAndDriverStatus(Role role, DriverStatus driverStatus);

    List<User> findByRole(Role role);

    Boolean existsByEmail(String email);

    Boolean existsByPhoneNumber(String phoneNumber);
}
