package ride.sharing.com.impl;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ride.sharing.com.dtos.UserDto;
import ride.sharing.com.enums.DriverStatus;
import ride.sharing.com.enums.Role;
import ride.sharing.com.exception.DriverNotAvailableException;
import ride.sharing.com.exception.DuplicateResourceException;
import ride.sharing.com.exception.ResourceNotFoundException;
import ride.sharing.com.models.User;
import ride.sharing.com.repositories.UserRepository;
import ride.sharing.com.services.UserService;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ModelMapper modelMapper;

    @Override
    public User registerUser(UserDto.Create create) {
        log.info("Registering new user with email: {}", create.getEmail());

        if (userRepository.existsByEmail(create.getEmail())) {
            throw new DuplicateResourceException("Email already exists");
        }
        if (userRepository.existsByPhoneNumber(create.getPhoneNumber())) {
            throw new DuplicateResourceException("Mobile number already exists");
        }
        User user = modelMapper.map(create, User.class);
        /*
        Driver has to be vetted by admin
         */
        if (create.getRole().equals(Role.DRIVER)) {
            user.setActive(false);
        }
        user.setPassword(passwordEncoder.encode(create.getPassword()));

        // Set default driver status if role is DRIVER
        if (user.getRole() == Role.DRIVER && user.getDriverStatus() == null) {
            user.setDriverStatus(DriverStatus.OFFLINE);
        }

        User savedUser = userRepository.save(user);
        log.info("User registered successfully with ID: {}", savedUser.getId());
        return savedUser;
    }

    @Override
    @Transactional(readOnly = true)
    public User findByEmail(String email) {
        log.debug("Finding user by email: {}", email);
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
    }

    @Override
    @Transactional(readOnly = true)
    public User findById(Long id) {
        log.debug("Finding user by ID: {}", id);
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> getAvailableDrivers() {
        log.debug("Fetching all available drivers");
        return userRepository.findByRoleAndDriverStatus(Role.DRIVER, DriverStatus.AVAILABLE);
    }

    @Override
    public User updateDriverStatus(Long driverId, DriverStatus status) {
        log.info("Updating driver {} status to {}", driverId, status);

        User driver = findById(driverId);

        if (driver.getRole() != Role.DRIVER) {
            throw new DriverNotAvailableException("User is not a driver");
        }

        driver.setDriverStatus(status);
        User updatedDriver = userRepository.save(driver);

        log.info("Driver status updated successfully");
        return updatedDriver;
    }

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public List<User> getAllDrivers() {
        return userRepository.findByRole(Role.DRIVER);
    }

    @Override
    public User updateUserStatus(boolean status, Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User with Id " + id + " not found"));
        user.setActive(status);
        return userRepository.save(user);
    }

    @Override
    public User updateUser(Long userId, UserDto.Update updateDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User with Id " + userId + " not found"));
        modelMapper.map(updateDto, user);
        user.setId(userId);
        return userRepository.save(user);
    }
}
