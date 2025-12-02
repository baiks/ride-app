package ride.sharing.com.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ride.sharing.com.enums.DriverStatus;
import ride.sharing.com.enums.Role;
import ride.sharing.com.exception.ResourceNotFoundException;
import ride.sharing.com.models.User;
import ride.sharing.com.services.LocationService;
import ride.sharing.com.services.MatchingService;
import ride.sharing.com.services.UserService;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class MatchingServiceImpl implements MatchingService {

    private final LocationService locationService;
    private final UserService userService;

    private static final double INITIAL_SEARCH_RADIUS_KM = 5.0;
    private static final double MAX_SEARCH_RADIUS_KM = 20.0;
    private static final double RADIUS_INCREMENT_KM = 5.0;

    @Override
    public Long findNearestDriver(Double pickupLat, Double pickupLng) {
        log.info("Finding nearest driver for location: ({}, {})", pickupLat, pickupLng);

        double currentRadius = INITIAL_SEARCH_RADIUS_KM;

        while (currentRadius <= MAX_SEARCH_RADIUS_KM) {
            log.debug("Searching within {}km radius", currentRadius);

            List<Long> nearbyDriverIds = locationService.findNearbyDrivers(
                    pickupLat,
                    pickupLng,
                    currentRadius
            );

            if (!nearbyDriverIds.isEmpty()) {
                // Verify driver is actually available
                for (Long driverId : nearbyDriverIds) {
                    try {
                        User driver = userService.findById(driverId);
                        if (driver.getRole() == Role.DRIVER &&
                                driver.getDriverStatus() == DriverStatus.AVAILABLE) {

                            Double distance = locationService.getDistance(driverId, pickupLat, pickupLng);
                            log.info("Found available driver {} at distance {}km", driverId, distance);
                            return driverId;
                        }
                    } catch (Exception e) {
                        log.warn("Driver {} not found or unavailable, skipping", driverId);
                    }
                }
            }

            currentRadius += RADIUS_INCREMENT_KM;
        }

        log.error("No available drivers found within {}km", MAX_SEARCH_RADIUS_KM);
        throw new ResourceNotFoundException("No drivers available nearby. Please try again later.");
    }

    @Override
    public List<Long> findTopNearestDrivers(Double pickupLat, Double pickupLng, int count) {
        log.info("Finding top {} nearest drivers for location: ({}, {})", count, pickupLat, pickupLng);

        List<Long> nearbyDriverIds = locationService.findNearbyDrivers(
                pickupLat,
                pickupLng,
                MAX_SEARCH_RADIUS_KM
        );

        // Filter only available drivers
        List<Long> availableDrivers = nearbyDriverIds.stream()
                .filter(driverId -> {
                    try {
                        User driver = userService.findById(driverId);
                        return driver.getRole() == Role.DRIVER &&
                                driver.getDriverStatus() == DriverStatus.AVAILABLE;
                    } catch (Exception e) {
                        return false;
                    }
                })
                .limit(count)
                .toList();

        log.info("Found {} available drivers", availableDrivers.size());
        return availableDrivers;
    }
}
