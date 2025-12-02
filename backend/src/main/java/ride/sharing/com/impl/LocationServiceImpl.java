package ride.sharing.com.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.geo.*;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.GeoOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import ride.sharing.com.services.LocationService;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class LocationServiceImpl implements LocationService {

    private final RedisTemplate<String, String> redisTemplate;
    private static final String GEO_KEY = "driver:locations";

    @Override
    public void updateDriverLocation(Long driverId, Double lat, Double lng) {
        log.debug("Updating location for driver {}: ({}, {})", driverId, lat, lng);

        GeoOperations<String, String> geoOps = redisTemplate.opsForGeo();
        Point point = new Point(lng, lat);

        Long added = geoOps.add(GEO_KEY, point, driverId.toString());

        if (added != null && added > 0) {
            log.info("Driver {} location updated successfully", driverId);
        } else {
            log.info("Driver {} location already exists, updated position", driverId);
        }
    }

    @Override
    public List<Long> findNearbyDrivers(Double lat, Double lng, Double radiusKm) {
        log.debug("Searching for drivers within {}km of ({}, {})", radiusKm, lat, lng);

        GeoOperations<String, String> geoOps = redisTemplate.opsForGeo();

        // Create circle for search area
        Circle circle = new Circle(
                new Point(lng, lat),
                new Distance(radiusKm, Metrics.KILOMETERS)
        );

        // Create GeoRadiusCommandArgs
        RedisGeoCommands.GeoRadiusCommandArgs args = RedisGeoCommands.GeoRadiusCommandArgs
                .newGeoRadiusArgs()
                .includeDistance()
                .sortAscending()
                .limit(10);

        // Search for nearby drivers
        GeoResults<RedisGeoCommands.GeoLocation<String>> results = geoOps.radius(
                GEO_KEY,
                circle,
                args
        );

        if (results == null || results.getContent().isEmpty()) {
            log.warn("No drivers found within {}km", radiusKm);
            return List.of();
        }

        List<Long> driverIds = results.getContent().stream()
                .map(result -> {
                    String driverIdStr = result.getContent().getName();
                    Distance distance = result.getDistance();
                    log.debug("Found driver {} at distance {}km", driverIdStr,
                            distance != null ? distance.getValue() : "unknown");
                    return Long.parseLong(driverIdStr);
                })
                .collect(Collectors.toList());

        log.info("Found {} nearby drivers", driverIds.size());
        return driverIds;
    }

    @Override
    public void removeDriverLocation(Long driverId) {
        log.info("Removing location for driver {}", driverId);

        GeoOperations<String, String> geoOps = redisTemplate.opsForGeo();
        Long removed = geoOps.remove(GEO_KEY, driverId.toString());

        if (removed != null && removed > 0) {
            log.info("Driver {} location removed successfully", driverId);
        } else {
            log.warn("Driver {} location not found in cache", driverId);
        }
    }

    @Override
    public Double getDistance(Long driverId, Double lat, Double lng) {
        log.debug("Calculating distance for driver {} from ({}, {})", driverId, lat, lng);

        GeoOperations<String, String> geoOps = redisTemplate.opsForGeo();

        // Get driver's current position
        List<Point> positions = geoOps.position(GEO_KEY, driverId.toString());

        if (positions == null || positions.isEmpty() || positions.get(0) == null) {
            log.warn("Driver {} location not found in cache", driverId);
            return null;
        }

        Point driverPoint = positions.get(0);

        // Calculate distance manually using Haversine formula
        double distance = calculateHaversineDistance(
                driverPoint.getY(), driverPoint.getX(),
                lat, lng
        );

        log.debug("Distance: {}km", distance);
        return distance;
    }

    /**
     * Calculate distance between two coordinates using Haversine formula
     * @param lat1 Latitude of point 1
     * @param lon1 Longitude of point 1
     * @param lat2 Latitude of point 2
     * @param lon2 Longitude of point 2
     * @return Distance in kilometers
     */
    private double calculateHaversineDistance(double lat1, double lon1, double lat2, double lon2) {
        final int EARTH_RADIUS_KM = 6371;

        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS_KM * c;
    }
}