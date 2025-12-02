package ride.sharing.com.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ride.sharing.com.dtos.LocationUpdate;
import ride.sharing.com.services.LocationService;

@RestController
@RequestMapping("/api/location")
@RequiredArgsConstructor
@Tag(name = "Location", description = "Real-time Location Tracking APIs")
public class LocationController {
    private final LocationService locationService;

    @PostMapping("/update")
    @Operation(
            summary = "Update driver location",
            description = "Driver updates their real-time location (stored in Redis with geospatial indexing)"
    )
    public ResponseEntity<?> updateLocation(
            @Parameter(description = "Driver ID") @RequestParam Long driverId,
            @RequestBody LocationUpdate location
    ) {
        locationService.updateDriverLocation(
                driverId,
                location.getLatitude(),
                location.getLongitude()
        );
        return ResponseEntity.ok("Location updated");
    }
}
