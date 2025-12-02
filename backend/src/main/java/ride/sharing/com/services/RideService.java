package ride.sharing.com.services;

import ride.sharing.com.dtos.RideRequest;
import ride.sharing.com.models.Ride;

import java.util.List;

public interface RideService {
    Ride requestRide(Long customerId, RideRequest request);
    Ride acceptRide(Long rideId, Long driverId);
    Ride startRide(Long rideId);
    Ride completeRide(Long rideId);
    Ride cancelRide(Long rideId, String reason);
    List<Ride> getCustomerRides(Long customerId);
    List<Ride> getDriverRides(Long driverId);
    Ride getRideById(Long rideId);
    List<Ride> getPendingRides();
    List<Ride> getAllRides();
}
