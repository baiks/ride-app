package ride.sharing.com.dtos;

import lombok.Data;

@Data
public class RideRequest {
    private Double pickupLat;
    private Double pickupLng;
    private String pickupAddress;
    private Double dropoffLat;
    private Double dropoffLng;
    private String dropoffAddress;
}
