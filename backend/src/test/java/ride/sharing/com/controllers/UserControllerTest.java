package ride.sharing.com.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.ResponseEntity;
import ride.sharing.com.dtos.UserDto;
import ride.sharing.com.enums.DriverStatus;
import ride.sharing.com.models.User;
import ride.sharing.com.services.UserService;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class UserControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    private User sampleUser;
    private List<User> sampleUsers;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        sampleUser = new User();
        sampleUser.setFirstName("John");
        sampleUser.setLastName("Driver");
        sampleUser.setEmail("driver1@ridesharingapp.com");
        sampleUsers = List.of(sampleUser);
    }

    @Test
    void getAvailableDrivers_shouldReturnListOfDrivers() {
        when(userService.getAvailableDrivers()).thenReturn(sampleUsers);
        ResponseEntity<List<User>> response = userController.getAvailableDrivers();
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).isEqualTo(sampleUsers);
        verify(userService, times(1)).getAvailableDrivers();
    }

    @Test
    void updateDriverStatus_shouldReturnUpdatedUser() {
        when(userService.updateDriverStatus(1L, DriverStatus.AVAILABLE)).thenReturn(sampleUser);
        ResponseEntity<User> response = userController.updateDriverStatus(1L, DriverStatus.AVAILABLE);
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).isEqualTo(sampleUser);
        verify(userService, times(1)).updateDriverStatus(1L, DriverStatus.AVAILABLE);
    }

    @Test
    void getAllDrivers_shouldReturnListOfAllDrivers() {
        when(userService.getAllDrivers()).thenReturn(sampleUsers);
        ResponseEntity<List<User>> response = userController.getAllDrivers();
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).isEqualTo(sampleUsers);
        verify(userService, times(1)).getAllDrivers();
    }

    @Test
    void getAllUsers_shouldReturnListOfUsers() {
        when(userService.getAllUsers()).thenReturn(sampleUsers);
        ResponseEntity<List<User>> response = userController.getAllUsers();
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).isEqualTo(sampleUsers);
        verify(userService, times(1)).getAllUsers();
    }

    @Test
    void updateUserStatus_shouldReturnUpdatedUser() {
        when(userService.updateUserStatus(true, 1L)).thenReturn(sampleUser);
        ResponseEntity<User> response = userController.updateUserStatus(1L, true);
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).isEqualTo(sampleUser);
        verify(userService, times(1)).updateUserStatus(true, 1L);
    }

    @Test
    void updateUser_shouldReturnUpdatedUser() {
        UserDto.Update updateDto = new UserDto.Update();
        // Set fields in updateDto as needed for the test
        when(userService.updateUser(eq(1L), any(UserDto.Update.class))).thenReturn(sampleUser);
        ResponseEntity<User> response = userController.updateUser(1L, updateDto);
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).isEqualTo(sampleUser);
        verify(userService, times(1)).updateUser(eq(1L), any(UserDto.Update.class));
    }
}
