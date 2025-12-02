package ride.sharing.com.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import ride.sharing.com.dtos.UserDto;
import ride.sharing.com.enums.DriverStatus;
import ride.sharing.com.enums.Role;
import ride.sharing.com.exception.DriverNotAvailableException;
import ride.sharing.com.exception.DuplicateResourceException;
import ride.sharing.com.exception.ResourceNotFoundException;
import ride.sharing.com.impl.UserServiceImpl;
import ride.sharing.com.models.User;
import ride.sharing.com.repositories.UserRepository;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private UserServiceImpl userService;

    private User user;
    private UserDto.Create createDto;

    @BeforeEach
    void setup() {
        user = User.builder()
                .id(1L)
                .email("test@example.com")
                .phoneNumber("+1234567890")
                .role(Role.DRIVER)
                .driverStatus(DriverStatus.OFFLINE)
                .active(true)
                .build();

        createDto = new UserDto.Create();
        createDto.setEmail("test@example.com");
        createDto.setPhoneNumber("+1234567890");
        createDto.setRole(Role.DRIVER);
        createDto.setPassword("password123");
    }

    @Test
    void registerUser_successful() {
        when(userRepository.existsByEmail(createDto.getEmail())).thenReturn(false);
        when(userRepository.existsByPhoneNumber(createDto.getPhoneNumber())).thenReturn(false);
        when(modelMapper.map(createDto, User.class)).thenReturn(user);
        when(passwordEncoder.encode(createDto.getPassword())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArguments()[0]);

        User result = userService.registerUser(createDto);

        assertThat(result.getEmail()).isEqualTo(createDto.getEmail());
        assertThat(result.getPassword()).isEqualTo("encodedPassword");
        assertThat(result.getActive()).isFalse();  // Because role is DRIVER, active = false
        assertThat(result.getDriverStatus()).isEqualTo(DriverStatus.OFFLINE);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void registerUser_duplicateEmail_throwsException() {
        when(userRepository.existsByEmail(createDto.getEmail())).thenReturn(true);

        assertThatThrownBy(() -> userService.registerUser(createDto))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessage("Email already exists");

        verify(userRepository, never()).save(any());
    }

    @Test
    void registerUser_duplicatePhone_throwsException() {
        when(userRepository.existsByEmail(createDto.getEmail())).thenReturn(false);
        when(userRepository.existsByPhoneNumber(createDto.getPhoneNumber())).thenReturn(true);

        assertThatThrownBy(() -> userService.registerUser(createDto))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessage("Mobile number already exists");

        verify(userRepository, never()).save(any());
    }

    @Test
    void findByEmail_successful() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));

        User result = userService.findByEmail("test@example.com");

        assertThat(result).isEqualTo(user);
    }

    @Test
    void findByEmail_notFound_throwsException() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.findByEmail("test@example.com"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User not found with email");
    }

    @Test
    void findById_successful() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        User result = userService.findById(1L);

        assertThat(result).isEqualTo(user);
    }

    @Test
    void findById_notFound_throwsException() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.findById(1L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User not found with ID");
    }

    @Test
    void getAvailableDrivers_returnsList() {
        List<User> drivers = List.of(user);
        when(userRepository.findByRoleAndDriverStatus(Role.DRIVER, DriverStatus.AVAILABLE)).thenReturn(drivers);

        List<User> result = userService.getAvailableDrivers();

        assertThat(result).isEqualTo(drivers);
    }

    @Test
    void updateDriverStatus_successful() {
        user.setRole(Role.DRIVER);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);

        User result = userService.updateDriverStatus(1L, DriverStatus.BUSY);

        assertThat(result.getDriverStatus()).isEqualTo(DriverStatus.BUSY);
        verify(userRepository).save(user);
    }

    @Test
    void updateDriverStatus_notDriver_throwsException() {
        user.setRole(Role.CUSTOMER);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> userService.updateDriverStatus(1L, DriverStatus.AVAILABLE))
                .isInstanceOf(DriverNotAvailableException.class)
                .hasMessage("User is not a driver");
    }

    @Test
    void getAllUsers_returnsList() {
        List<User> users = List.of(user);
        when(userRepository.findAll()).thenReturn(users);

        List<User> result = userService.getAllUsers();

        assertThat(result).isEqualTo(users);
    }

    @Test
    void getAllDrivers_returnsList() {
        List<User> drivers = List.of(user);
        when(userRepository.findByRole(Role.DRIVER)).thenReturn(drivers);

        List<User> result = userService.getAllDrivers();

        assertThat(result).isEqualTo(drivers);
    }

    @Test
    void updateUserStatus_successful() {
        user.setActive(false);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);

        User result = userService.updateUserStatus(true, 1L);

        assertThat(result.getActive()).isTrue();
        verify(userRepository).save(user);
    }

    @Test
    void updateUserStatus_notFound_throwsException() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.updateUserStatus(true, 1L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User with Id 1 not found");
    }

    @Test
    void updateUser_successful() {
        UserDto.Update updateDto = new UserDto.Update();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);
        doNothing().when(modelMapper).map(updateDto, user);

        User result = userService.updateUser(1L, updateDto);

        assertThat(result).isEqualTo(user);
        verify(modelMapper).map(updateDto, user);
        verify(userRepository).save(user);
    }

    @Test
    void updateUser_notFound_throwsException() {
        UserDto.Update updateDto = new UserDto.Update();
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.updateUser(1L, updateDto))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User with Id 1 not found");
    }
}
