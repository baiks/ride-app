package ride.sharing.com.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ride.sharing.com.dtos.RideRequest;
import ride.sharing.com.enums.DriverStatus;
import ride.sharing.com.enums.RideStatus;
import ride.sharing.com.enums.Role;
import ride.sharing.com.exception.DriverNotAvailableException;
import ride.sharing.com.exception.ForbiddenException;
import ride.sharing.com.exception.RideNotAvailableException;
import ride.sharing.com.models.Ride;
import ride.sharing.com.models.User;
import ride.sharing.com.repositories.RideRepository;
import ride.sharing.com.services.LocationService;
import ride.sharing.com.services.MatchingService;
import ride.sharing.com.services.RideService;
import ride.sharing.com.services.UserService;
import ride.sharing.com.services.LocationService;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class RideServiceImpl implements RideService {

    private final RideRepository rideRepository;
    private final UserService userService;
    private final MatchingService matchingService;
    private final LocationService locationService;

    @Override
    public Ride requestRide(Long customerId, RideRequest request) {
        log.info("Processing ride request from customer {}", customerId);

        User customer = userService.findById(customerId);

        if (customer.getRole() != Role.CUSTOMER) {
            throw new RuntimeException("Only customers can request rides");
        }

        Ride ride = new Ride();
        ride.setCustomer(customer);
        ride.setPickupLat(request.getPickupLat());
        ride.setPickupLng(request.getPickupLng());
        ride.setPickupAddress(request.getPickupAddress());
        ride.setDropoffLat(request.getDropoffLat());
        ride.setDropoffLng(request.getDropoffLng());
        ride.setDropoffAddress(request.getDropoffAddress());
        ride.setStatus(RideStatus.REQUESTED);
        ride.setRequestedAt(LocalDateTime.now());

        // Calculate estimated distance (simple Haversine formula could be used)
        double distance = calculateDistance(
                request.getPickupLat(), request.getPickupLng(),
                request.getDropoffLat(), request.getDropoffLng()
        );
        ride.setDistance(distance);

        // Calculate estimated fare (simple pricing: $2 base + $1.5 per km)
        double fare = 2.0 + (distance * 1.5);
        ride.setFare(fare);

        // Try to find and assign a driver automatically
        try {
            Long driverId = matchingService.findNearestDriver(
                    request.getPickupLat(),
                    request.getPickupLng()
            );
            User driver = userService.findById(driverId);
            ride.setDriver(driver);
            log.info("Automatically assigned driver {} to ride", driverId);
        } catch (Exception e) {
            log.warn("Could not auto-assign driver: {}", e.getMessage());
            // Ride will be saved with status REQUESTED and no driver assigned
        }

        Ride savedRide = rideRepository.save(ride);
        log.info("Ride created successfully with ID: {}", savedRide.getId());

        return savedRide;
    }

    @Override
    public Ride acceptRide(Long rideId, Long driverId) {
        log.info("Driver {} accepting ride {}", driverId, rideId);

        Ride ride = getRideById(rideId);

        if (ride.getStatus() != RideStatus.REQUESTED) {
            throw new RideNotAvailableException("Ride is not available for acceptance");
        }

        User driver = userService.findById(driverId);

        if (driver.getRole() != Role.DRIVER) {
            throw new ForbiddenException("Only drivers can accept rides");
        }

        if (driver.getDriverStatus() != DriverStatus.AVAILABLE) {
            throw new DriverNotAvailableException("Driver is not available");
        }

        ride.setDriver(driver);
        ride.setStatus(RideStatus.ACCEPTED);
        ride.setAcceptedAt(LocalDateTime.now());

        // Update driver status to BUSY
        userService.updateDriverStatus(driverId, DriverStatus.BUSY);

        Ride updatedRide = rideRepository.save(ride);
        log.info("Ride {} accepted by driver {}", rideId, driverId);

        return updatedRide;
    }

    @Override
    public Ride startRide(Long rideId) {
        log.info("Starting ride {}", rideId);

        Ride ride = getRideById(rideId);

        if (ride.getStatus() != RideStatus.ACCEPTED) {
            throw new ForbiddenException("Ride must be accepted before starting");
        }

        ride.setStatus(RideStatus.IN_PROGRESS);

        Ride updatedRide = rideRepository.save(ride);
        log.info("Ride {} started", rideId);

        return updatedRide;
    }

    @Override
    public Ride completeRide(Long rideId) {
        log.info("Completing ride {}", rideId);

        Ride ride = getRideById(rideId);

        if (ride.getStatus() != RideStatus.IN_PROGRESS &&
                ride.getStatus() != RideStatus.ACCEPTED) {
            throw new ForbiddenException("Cannot complete ride in current status");
        }

        ride.setStatus(RideStatus.COMPLETED);
        ride.setCompletedAt(LocalDateTime.now());

        // Update driver status back to AVAILABLE
        if (ride.getDriver() != null) {
            userService.updateDriverStatus(
                    ride.getDriver().getId(),
                    DriverStatus.AVAILABLE
            );

            //Update driver current location
            locationService.updateDriverLocation(ride.getDriver().getId(), ride.getDropoffLat(), ride.getDropoffLng());
        }
        Ride updatedRide = rideRepository.save(ride);
        log.info("Ride {} completed", rideId);

        return updatedRide;
    }

    @Override
    public Ride cancelRide(Long rideId, String reason) {
        log.info("Cancelling ride {} with reason: {}", rideId, reason);

        Ride ride = getRideById(rideId);

        if (ride.getStatus() == RideStatus.COMPLETED) {
            throw new RuntimeException("Cannot cancel a completed ride");
        }

        ride.setStatus(RideStatus.CANCELLED);

        // If driver was assigned, make them available again
        if (ride.getDriver() != null &&
                ride.getDriver().getDriverStatus() == DriverStatus.BUSY) {
            userService.updateDriverStatus(
                    ride.getDriver().getId(),
                    DriverStatus.AVAILABLE
            );
        }

        Ride updatedRide = rideRepository.save(ride);
        log.info("Ride {} cancelled", rideId);

        return updatedRide;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Ride> getCustomerRides(Long customerId) {
        log.debug("Fetching rides for customer {}", customerId);
        return rideRepository.findByCustomerId(customerId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Ride> getDriverRides(Long driverId) {
        log.debug("Fetching rides for driver {}", driverId);
        return rideRepository.findByDriverId(driverId);
    }

    @Override
    @Transactional(readOnly = true)
    public Ride getRideById(Long rideId) {
        log.debug("Fetching ride {}", rideId);
        return rideRepository.findById(rideId)
                .orElseThrow(() -> new RideNotAvailableException("Ride not found with ID: " + rideId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Ride> getPendingRides() {
        log.debug("Fetching pending rides");
        return rideRepository.findByStatus(RideStatus.REQUESTED);
    }

    @Override
    public List<Ride> getAllRides() {
        return rideRepository.findAll();
    }

    /**
     * Calculate distance between two points using Haversine formula
     * Returns distance in kilometers
     */
    private double calculateDistance(Double lat1, Double lon1, Double lat2, Double lon2) {
        final int EARTH_RADIUS = 6371; // Radius in kilometers

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);

        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS * c;
    }
}
