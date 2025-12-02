package ride.sharing.com.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import ride.sharing.com.dtos.UserDto;
import ride.sharing.com.enums.Role;
import ride.sharing.com.models.User;
import ride.sharing.com.security.JwtUtil;
import ride.sharing.com.services.CustomUserDetailsService;
import ride.sharing.com.services.UserService;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
class AuthControllerTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserService userService;

    @Mock
    private CustomUserDetailsService userDetailsService;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthController authController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(authController).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void register_ShouldReturnCreatedUser_WhenValidRequest() throws Exception {
        // Arrange
        UserDto.Create createDto = new UserDto.Create();
        createDto.setEmail("test@example.com");
        createDto.setPassword("Password@123");
        createDto.setFirstName("John");
        createDto.setLastName("Doe");
        createDto.setPhoneNumber("+1234567890");
        createDto.setRole(Role.CUSTOMER);

        User savedUser = User.builder()
                .id(1L)
                .email("test@example.com")
                .firstName("John")
                .lastName("Doe")
                .phoneNumber("+1234567890")
                .role(Role.CUSTOMER)
                .build();


        when(userService.registerUser(any(UserDto.Create.class))).thenReturn(savedUser);

        // Act & Assert
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Doe"))
                .andExpect(jsonPath("$.role").value("CUSTOMER"));

        verify(userService, times(1)).registerUser(any(UserDto.Create.class));
    }

    @Test
    void register_ShouldReturnBadRequest_WhenInvalidEmail() throws Exception {
        // Arrange
        UserDto.Create createDto = new UserDto.Create();
        createDto.setEmail("invalid-email");
        createDto.setPassword("Password@123");
        createDto.setFirstName("John");
        createDto.setLastName("Doe");

        // Act & Assert
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isBadRequest());

        verify(userService, never()).registerUser(any(UserDto.Create.class));
    }

    @Test
    void login_ShouldReturnTokenAndUser_WhenValidCredentials() throws Exception {
        // Arrange
        UserDto.Login loginDto = new UserDto.Login();
        loginDto.setEmail("test@example.com");
        loginDto.setPassword("Password@123");

        Authentication authentication = mock(Authentication.class);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);

        UserDetails userDetails = org.springframework.security.core.userdetails.User
                .withUsername("test@example.com")
                .password("encodedPassword")
                .authorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_CUSTOMER")))
                .build();

        when(userDetailsService.loadUserByUsername("test@example.com"))
                .thenReturn(userDetails);

        when(jwtUtil.generateToken(userDetails)).thenReturn("mock-jwt-token");

        User user = User.builder()
                .id(1L)
                .email("test@example.com")
                .firstName("John")
                .lastName("Doe")
                .role(Role.CUSTOMER)
                .build();


        when(userService.findByEmail("test@example.com")).thenReturn(user);

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("mock-jwt-token"))
                .andExpect(jsonPath("$.user.id").value(1))
                .andExpect(jsonPath("$.user.email").value("test@example.com"))
                .andExpect(jsonPath("$.user.firstName").value("John"))
                .andExpect(jsonPath("$.user.role").value("CUSTOMER"));

        verify(authenticationManager, times(1))
                .authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userDetailsService, times(1)).loadUserByUsername("test@example.com");
        verify(jwtUtil, times(1)).generateToken(userDetails);
        verify(userService, times(1)).findByEmail("test@example.com");
    }

    @Test
    void login_ShouldReturnUnauthorized_WhenInvalidCredentials() throws Exception {
        // Arrange
        UserDto.Login loginDto = new UserDto.Login();
        loginDto.setEmail("test@example.com");
        loginDto.setPassword("wrongpassword");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Invalid credentials"));

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDto)))
                .andExpect(status().isUnauthorized());

        verify(authenticationManager, times(1))
                .authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userDetailsService, never()).loadUserByUsername(anyString());
        verify(jwtUtil, never()).generateToken(any(UserDetails.class));
        verify(userService, never()).findByEmail(anyString());
    }

    @Test
    void login_ShouldReturnUnauthorized_WhenUserNotFound() throws Exception {
        // Arrange
        UserDto.Login loginDto = new UserDto.Login();
        loginDto.setEmail("nonexistent@example.com");
        loginDto.setPassword("Password@123");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new UsernameNotFoundException("User not found with email: " + loginDto.getEmail()));

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDto)))
                .andExpect(status().isUnauthorized());

        verify(authenticationManager, times(1))
                .authenticate(any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    void register_ShouldRegisterDriver_WhenRoleIsDriver() throws Exception {
        // Arrange
        UserDto.Create createDto = new UserDto.Create();
        createDto.setEmail("driver@example.com");
        createDto.setPassword("Password@123");
        createDto.setFirstName("Jane");
        createDto.setLastName("Driver");
        createDto.setPhoneNumber("+1234567890");
        createDto.setRole(Role.DRIVER);

        User savedUser = User.builder()
                .id(2L)
                .email("driver@example.com")
                .firstName("Jane")
                .lastName("Driver")
                .role(Role.DRIVER)
                .build();


        when(userService.registerUser(any(UserDto.Create.class))).thenReturn(savedUser);

        // Act & Assert
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.email").value("driver@example.com"))
                .andExpect(jsonPath("$.role").value("DRIVER"));

        verify(userService, times(1)).registerUser(any(UserDto.Create.class));
    }

    @Test
    void register_ShouldRegisterAdmin_WhenRoleIsAdmin() throws Exception {
        // Arrange
        UserDto.Create createDto = new UserDto.Create();
        createDto.setEmail("admin@example.com");
        createDto.setPassword("Passwor@d123");
        createDto.setFirstName("Admin");
        createDto.setLastName("User");
        createDto.setPhoneNumber("+1234567890");
        createDto.setRole(Role.ADMIN);

        User savedUser = User.builder()
                .id(3L)
                .email("admin@example.com")
                .firstName("Admin")
                .lastName("User")
                .role(Role.ADMIN)
                .build();


        when(userService.registerUser(any(UserDto.Create.class))).thenReturn(savedUser);

        // Act & Assert
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(3))
                .andExpect(jsonPath("$.email").value("admin@example.com"))
                .andExpect(jsonPath("$.role").value("ADMIN"));

        verify(userService, times(1)).registerUser(any(UserDto.Create.class));
    }
}