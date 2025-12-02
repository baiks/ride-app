package ride.sharing.com.services;

import java.util.List;

public interface MatchingService {
    Long findNearestDriver(Double pickupLat, Double pickupLng);
    List<Long> findTopNearestDrivers(Double pickupLat, Double pickupLng, int count);
}
