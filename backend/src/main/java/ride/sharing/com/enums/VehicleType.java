package ride.sharing.com.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum VehicleType {
    // Economy Options
    UBER_GO("UberGo", "Affordable, everyday rides", 4, 1.0),
    UBER_X("UberX", "Affordable rides for up to 4 passengers", 4, 1.2),
    
    // Premium Options
    UBER_COMFORT("UberComfort", "Newer cars with extra legroom", 4, 1.5),
    UBER_BLACK("UberBlack", "Premium rides in luxury cars", 4, 2.0),
    UBER_BLACK_SUV("UberBlack SUV", "Premium SUV rides for groups", 6, 2.5),
    
    // Large Group Options
    UBER_XL("UberXL", "Affordable rides for up to 6 passengers", 6, 1.8),
    UBER_SUV("UberSUV", "Premium SUV for larger groups", 6, 2.3),
    
    // Motorcycle/Bike Options
    UBER_MOTO("UberMoto", "Affordable motorcycle rides", 1, 0.5),
    UBER_AUTO("UberAuto", "Auto rickshaw rides", 3, 0.7),
    
    // Electric/Eco Options
    UBER_GREEN("UberGreen", "Eco-friendly hybrid or electric vehicles", 4, 1.3),
    
    // Luxury Options
    UBER_LUX("UberLux", "High-end luxury car experience", 4, 3.0),
    UBER_LUX_SUV("UberLux SUV", "Top-tier luxury SUV", 6, 3.5),
    
    // Accessibility
    UBER_WAV("UberWAV", "Wheelchair accessible vehicles", 4, 1.5),
    
    // Shared Rides
    UBER_POOL("UberPool", "Share your ride, split the cost", 2, 0.8);

    private final String displayName;
    private final String description;
    private final int maxPassengers;
    private final double priceMultiplier; // Base multiplier for pricing

    VehicleType(String displayName, String description, int maxPassengers, double priceMultiplier) {
        this.displayName = displayName;
        this.description = description;
        this.maxPassengers = maxPassengers;
        this.priceMultiplier = priceMultiplier;
    }

    @JsonValue
    public String getDisplayName() {
        return displayName;
    }

    // Helper method to get enum from display name
    public static VehicleType fromDisplayName(String displayName) {
        for (VehicleType type : VehicleType.values()) {
            if (type.displayName.equalsIgnoreCase(displayName)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown vehicle type: " + displayName);
    }

    // Check if vehicle can accommodate passengers
    public boolean canAccommodate(int passengers) {
        return passengers <= this.maxPassengers;
    }

    // Get category for filtering
    public VehicleCategory getCategory() {
        return switch (this) {
            case UBER_GO, UBER_X, UBER_POOL -> VehicleCategory.ECONOMY;
            case UBER_COMFORT, UBER_BLACK, UBER_BLACK_SUV -> VehicleCategory.PREMIUM;
            case UBER_XL, UBER_SUV -> VehicleCategory.LARGE_GROUP;
            case UBER_MOTO, UBER_AUTO -> VehicleCategory.TWO_WHEELER;
            case UBER_GREEN -> VehicleCategory.ECO_FRIENDLY;
            case UBER_LUX, UBER_LUX_SUV -> VehicleCategory.LUXURY;
            case UBER_WAV -> VehicleCategory.ACCESSIBLE;
        };
    }

    public enum VehicleCategory {
        ECONOMY("Economy"),
        PREMIUM("Premium"),
        LARGE_GROUP("Large Group"),
        TWO_WHEELER("Bike/Auto"),
        ECO_FRIENDLY("Eco-Friendly"),
        LUXURY("Luxury"),
        ACCESSIBLE("Accessible");

        @Getter
        private final String displayName;

        VehicleCategory(String displayName) {
            this.displayName = displayName;
        }
    }
}