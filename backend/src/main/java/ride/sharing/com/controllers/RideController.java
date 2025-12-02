package ride.sharing.com.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ride.sharing.com.dtos.RideRequest;
import ride.sharing.com.models.Ride;
import ride.sharing.com.services.RideService;

import java.util.List;

@RestController
@RequestMapping("/api/rides")
@RequiredArgsConstructor
@Tag(name = "Rides", description = "Ride Management APIs - Request, Accept, Complete Rides")
public class RideController {
    private final RideService rideService;

    @PostMapping("/request")
    @Operation(
            summary = "Request a new ride",
            description = "Customer requests a ride and system matches with nearest available driver"
    )
    public ResponseEntity<Ride> requestRide(
            @RequestBody RideRequest request,
            @Parameter(description = "Customer ID") @RequestParam Long customerId
    ) {
        Ride ride = rideService.requestRide(customerId, request);
        return ResponseEntity.ok(ride);
    }

    @PutMapping("/{rideId}/accept")
    @Operation(
            summary = "Driver accepts a ride",
            description = "Driver accepts a ride request"
    )
    public ResponseEntity<Ride> acceptRide(
            @Parameter(description = "Ride ID") @PathVariable Long rideId,
            @Parameter(description = "Driver ID") @RequestParam Long driverId
    ) {
        Ride ride = rideService.acceptRide(rideId, driverId);
        return ResponseEntity.ok(ride);
    }

    @PutMapping("/{rideId}/start")
    @Operation(
            summary = "Start a ride",
            description = "Driver starts the ride after picking up the customer"
    )
    public ResponseEntity<Ride> startRide(
            @Parameter(description = "Ride ID") @PathVariable Long rideId
    ) {
        Ride ride = rideService.startRide(rideId);
        return ResponseEntity.ok(ride);
    }

    @PutMapping("/{rideId}/complete")
    @Operation(
            summary = "Complete a ride",
            description = "Mark a ride as completed"
    )
    public ResponseEntity<Ride> completeRide(
            @Parameter(description = "Ride ID") @PathVariable Long rideId
    ) {
        Ride ride = rideService.completeRide(rideId);
        return ResponseEntity.ok(ride);
    }

    @PutMapping("/{rideId}/cancel")
    @Operation(
            summary = "Cancel a ride",
            description = "Cancel a ride before it starts (by customer or driver)"
    )
    public ResponseEntity<Ride> cancelRide(
            @Parameter(description = "Ride ID") @PathVariable Long rideId,
            @Parameter(description = "Cancellation reason") @RequestParam(required = false) String reason
    ) {
        Ride ride = rideService.cancelRide(rideId, reason);
        return ResponseEntity.ok(ride);
    }

    @GetMapping("/customer/{customerId}")
    @Operation(
            summary = "Get customer's ride history",
            description = "Retrieve all rides for a specific customer"
    )
    public ResponseEntity<List<Ride>> getCustomerRides(
            @Parameter(description = "Customer ID") @PathVariable Long customerId
    ) {
        return ResponseEntity.ok(rideService.getCustomerRides(customerId));
    }

    @GetMapping("/driver/{driverId}")
    @Operation(
            summary = "Get driver's ride history",
            description = "Retrieve all rides for a specific driver"
    )
    public ResponseEntity<List<Ride>> getDriverRides(
            @Parameter(description = "Driver ID") @PathVariable Long driverId
    ) {
        return ResponseEntity.ok(rideService.getDriverRides(driverId));
    }

    @GetMapping
    @Operation(
            summary = "Get all ride history",
            description = "Retrieve all rides"
    )
    public ResponseEntity<List<Ride>> getAllRides(
    ) {
        return ResponseEntity.ok(rideService.getAllRides());
    }
}