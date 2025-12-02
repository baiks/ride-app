package ride.sharing.com.services;

import ride.sharing.com.dtos.UserDto;
import ride.sharing.com.enums.DriverStatus;
import ride.sharing.com.models.User;

import java.util.List;

public interface UserService {
    User registerUser(UserDto.Create create);

    User findByEmail(String email);

    User findById(Long id);

    List<User> getAvailableDrivers();

    User updateDriverStatus(Long driverId, DriverStatus status);

    List<User> getAllUsers();

    List<User> getAllDrivers();

    User updateUserStatus(boolean status, Long id);

    User updateUser(Long userId, UserDto.Update updateDto);
}
