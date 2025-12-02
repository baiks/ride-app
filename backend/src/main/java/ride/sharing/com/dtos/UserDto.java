package ride.sharing.com.dtos;


import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.*;
import lombok.*;
import ride.sharing.com.enums.DriverStatus;
import ride.sharing.com.enums.Role;
import ride.sharing.com.impl.NonEmptyValidation;
import ride.sharing.com.models.User;


import java.time.LocalDateTime;

@Getter
@Setter
public class UserDto {
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Create {

        @NotBlank(message = "Email is required")
        @Email(message = "Email should be valid")
        private String email;

        @NotBlank(message = "Password is required")
        @Size(min = 8, message = "Password must be at least 8 characters long")
        @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).*$",
                message = "Password must contain at least one uppercase letter, one lowercase letter, and one number")
        private String password;

        @NotBlank(message = "First name is required")
        @Size(min = 2, max = 100, message = "First name must be between 2 and 100 characters")
        private String firstName;

        @NotBlank(message = "Last name is required")
        @Size(min = 2, max = 100, message = "Last name must be between 2 and 100 characters")
        private String lastName;

        @Pattern(regexp = "^[+]?[0-9]{10,15}$", message = "Phone number should be valid")
        private String phoneNumber;

        @NotNull(message = "Role is required")
        private Role role;

        // Driver-specific fields (optional, required only if role is DRIVER)
        private String vehicleType;

        private String licensePlate;
    }


    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Update {

        @JsonInclude(JsonInclude.Include.NON_EMPTY)
        @NotNull(message = "Full name cannot be null", groups = {NonEmptyValidation.class})
        @Size(min = 2, max = 100, message = "Full name must be between 2 and 100 characters", groups = {NonEmptyValidation.class})
        private String fullName;

        @JsonInclude(JsonInclude.Include.NON_EMPTY)
        @NotNull(message = "Phone number cannot be null", groups = {NonEmptyValidation.class})
        @Pattern(regexp = "^[+]?[0-9]{10,15}$", message = "Phone number should be valid", groups = {NonEmptyValidation.class})
        private String phoneNumber;

        @JsonInclude(JsonInclude.Include.NON_EMPTY)
        @NotNull(message = "Email cannot be null", groups = {NonEmptyValidation.class})
        @Email(message = "Email should be valid", groups = {NonEmptyValidation.class})
        private String email;

        // Driver-specific fields
        @JsonInclude(JsonInclude.Include.NON_EMPTY)
        @NotNull(message = "Vehicle type cannot be null", groups = {NonEmptyValidation.class})
        private String vehicleType;

        @JsonInclude(JsonInclude.Include.NON_EMPTY)
        @NotNull(message = "License plate cannot be null", groups = {NonEmptyValidation.class})
        private String licensePlate;

        @JsonInclude(JsonInclude.Include.NON_EMPTY)
        @NotNull(message = "Driver status cannot be null", groups = {NonEmptyValidation.class})
        private DriverStatus driverStatus;
    }


    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Login {

        @NotBlank(message = "Email is required")
        @Email(message = "Email should be valid")
        private String email;

        @NotBlank(message = "Password is required")
        private String password;
    }
    @Builder
    @Getter
    @Setter
    public static class Response {
        private String status;
        private String message;
    }

}