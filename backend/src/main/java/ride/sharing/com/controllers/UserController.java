package ride.sharing.com.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ride.sharing.com.dtos.UserDto;
import ride.sharing.com.enums.DriverStatus;
import ride.sharing.com.models.User;
import ride.sharing.com.services.UserService;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "User and Driver Management APIs")
public class UserController {
    private final UserService userService;

    @GetMapping("/drivers")
    @Operation(
            summary = "Get all available drivers",
            description = "Retrieve list of all drivers with AVAILABLE status"
    )
    public ResponseEntity<List<User>> getAvailableDrivers() {
        return ResponseEntity.ok(userService.getAvailableDrivers());
    }

    @PutMapping("/drivers/{driverId}/status")
    @Operation(
            summary = "Update driver status",
            description = "Update driver status: AVAILABLE, BUSY, or OFFLINE"
    )
    public ResponseEntity<User> updateDriverStatus(
            @Parameter(description = "Driver ID") @PathVariable Long driverId,
            @Parameter(description = "Driver Status") @RequestParam DriverStatus status
    ) {
        User driver = userService.updateDriverStatus(driverId, status);
        return ResponseEntity.ok(driver);
    }

    @GetMapping("/drivers/all")
    @Operation(
            summary = "Get all drivers",
            description = "Retrieve list of all drivers"
    )
    public ResponseEntity<List<User>> getAllDrivers() {
        return ResponseEntity.ok(userService.getAllDrivers());
    }

    @GetMapping
    @Operation(
            summary = "Get all users",
            description = "Retrieve list of all users"
    )
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @PutMapping("/user/{userId}/status")
    @Operation(
            summary = "Update user status",
            description = "Update user status: TRUE, FALSE"
    )
    public ResponseEntity<User> updateUserStatus(
            @Parameter(description = "User ID") @PathVariable Long userId,
            @Parameter(description = "User Status") @RequestParam boolean status
    ) {
        User driver = userService.updateUserStatus(status, userId);
        return ResponseEntity.ok(driver);
    }

    @PatchMapping("/{userId}")
    @Operation(
            summary = "Update user",
            description = "Update user information. Only provided fields will be updated"
    )
    public ResponseEntity<User> updateUser(
            @Parameter(description = "User ID") @PathVariable Long userId,
            @Valid @RequestBody UserDto.Update updateDto
    ) {
        User updatedUser = userService.updateUser(userId, updateDto);
        return ResponseEntity.ok(updatedUser);
    }
}