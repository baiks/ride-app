package ride.sharing.com.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import ride.sharing.com.dtos.UserDto;
import ride.sharing.com.models.User;
import ride.sharing.com.services.UserService;
import ride.sharing.com.services.CustomUserDetailsService;
import ride.sharing.com.security.JwtUtil;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Tag(name = "Authentication", description = "Authentication and Registration APIs")
public class AuthController {
    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final CustomUserDetailsService userDetailsService;
    private final JwtUtil jwtUtil;

    @PostMapping("/register")
    @Operation(
            summary = "Register a new user",
            description = "Register a new user with role: ADMIN, DRIVER, or CUSTOMER"
    )
    @ApiResponse(responseCode = "200", description = "User registered successfully")
    public ResponseEntity<?> register(@RequestBody @Valid UserDto.Create create) {
        User savedUser = userService.registerUser(create);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedUser);
    }

    @PostMapping("/login")
    @Operation(
            summary = "User login",
            description = "Authenticate user and return JWT token"
    )
    @ApiResponse(responseCode = "200", description = "Login successful, returns JWT token and user details")
    public ResponseEntity<?> login(@RequestBody UserDto.Login login) {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        login.getEmail(),
                        login.getPassword()
                )
        );

        // Load UserDetails using CustomUserDetailsService
        UserDetails userDetails = userDetailsService.loadUserByUsername(login.getEmail());

        // Generate token with UserDetails (includes roles)
        String token = jwtUtil.generateToken(userDetails);

        // Get user entity for response
        User user = userService.findByEmail(login.getEmail());

        Map<String, Object> response = new HashMap<>();
        response.put("token", token);
        response.put("user", user);

        return ResponseEntity.ok(response);
    }
}