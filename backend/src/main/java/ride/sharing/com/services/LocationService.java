package ride.sharing.com.services;

import java.util.List;

public interface LocationService {
    void updateDriverLocation(Long driverId, Double lat, Double lng);
    List<Long> findNearbyDrivers(Double lat, Double lng, Double radiusKm);
    void removeDriverLocation(Long driverId);
    Double getDistance(Long driverId, Double lat, Double lng);
}
