package ride.sharing.com.models;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;
import ride.sharing.com.enums.RideStatus;

import java.time.LocalDateTime;

@Entity
@Table(name = "rides")
@Data
@Builder
public class Ride {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "customer_id", nullable = false)
    private User customer;

    @ManyToOne
    @JoinColumn(name = "driver_id")
    private User driver;

    private Double pickupLat;
    private Double pickupLng;
    private String pickupAddress;

    private Double dropoffLat;
    private Double dropoffLng;
    private String dropoffAddress;

    @Enumerated(EnumType.STRING)
    private RideStatus status; // REQUESTED, ACCEPTED, IN_PROGRESS, COMPLETED, CANCELLED

    private Double fare;
    private Double distance;

    private LocalDateTime requestedAt = LocalDateTime.now();
    private LocalDateTime acceptedAt;
    private LocalDateTime completedAt;


}
