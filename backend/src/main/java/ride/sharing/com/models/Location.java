package ride.sharing.com.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Location {
    private Long driverId;
    private Double latitude;
    private Double longitude;
    private Long timestamp;
}

