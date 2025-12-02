package ride.sharing.com.models;

import jakarta.persistence.*;
import lombok.Data;
import ride.sharing.com.enums.DriverStatus;
import ride.sharing.com.enums.Role;
import ride.sharing.com.enums.VehicleType;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Data
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    private String phoneNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role; // ADMIN, DRIVER, CUSTOMER

    @Enumerated(EnumType.STRING)
    private DriverStatus driverStatus; // AVAILABLE, BUSY, OFFLINE

    @Enumerated(EnumType.STRING)
    private VehicleType vehicleType;

    private String licensePlate;

    @Column(nullable = false)
    private Boolean active = true;

    private LocalDateTime createdAt = LocalDateTime.now();
}