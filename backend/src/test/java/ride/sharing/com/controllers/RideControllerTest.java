package ride.sharing.com.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import ride.sharing.com.dtos.RideRequest;
import ride.sharing.com.models.Ride;
import ride.sharing.com.models.User;
import ride.sharing.com.enums.RideStatus;
import ride.sharing.com.services.RideService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
class RideControllerTest {

    @Mock
    private RideService rideService;

    @InjectMocks
    private RideController rideController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    private Ride testRide;
    private RideRequest testRideRequest;
    private User testCustomer;
    private User testDriver;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(rideController).build();
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules(); // For LocalDateTime serialization

        // Setup test customer
        User testCustomer = User.builder()
                .id(1L)
                .email("customer@example.com")
                .firstName("John")
                .lastName("Doe")
                .build();


        // Setup test driver
        User testDriver = User.builder()
                .id(2L)
                .email("driver@example.com")
                .firstName("Jane")
                .lastName("Smith")
                .build();


        // Setup test ride request
        testRideRequest = new RideRequest();
        testRideRequest.setPickupLat(-1.2921);
        testRideRequest.setPickupLng(36.8219);
        testRideRequest.setDropoffLat(-1.3000);
        testRideRequest.setDropoffLng(36.8300);
        testRideRequest.setPickupAddress("Nairobi CBD");
        testRideRequest.setDropoffAddress("Westlands");

        Ride testRide = Ride.builder()
                .id(1L)
                .customer(testCustomer)
                .driver(testDriver)
                .pickupLat(-1.2921)
                .pickupLng(36.8219)
                .dropoffLat(-1.3000)
                .dropoffLng(36.8300)
                .pickupAddress("Nairobi CBD")
                .dropoffAddress("Westlands")
                .status(RideStatus.REQUESTED)
                .fare(500.00)
                .requestedAt(LocalDateTime.now())
                .build();

    }

    @Test
    void requestRide_ShouldCreateRide_WhenValidRequest() throws Exception {
        // Arrange
        Long customerId = 1L;
        when(rideService.requestRide(eq(customerId), any(RideRequest.class)))
                .thenReturn(testRide);

        // Act & Assert
        mockMvc.perform(post("/api/rides/request")
                        .param("customerId", String.valueOf(customerId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testRideRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.pickupAddress").value("Nairobi CBD"))
                .andExpect(jsonPath("$.dropoffAddress").value("Westlands"))
                .andExpect(jsonPath("$.status").value("REQUESTED"))
                .andExpect(jsonPath("$.fare").value(500.00));

        verify(rideService, times(1)).requestRide(eq(customerId), any(RideRequest.class));
    }

    @Test
    void requestRide_ShouldReturnBadRequest_WhenCustomerIdMissing() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/rides/request")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testRideRequest)))
                .andExpect(status().isBadRequest());

        verify(rideService, never()).requestRide(anyLong(), any(RideRequest.class));
    }

    @Test
    void acceptRide_ShouldAcceptRide_WhenValidDriverAndRide() throws Exception {
        // Arrange
        Long rideId = 1L;
        Long driverId = 2L;
        testRide.setStatus(RideStatus.ACCEPTED);
        testRide.setAcceptedAt(LocalDateTime.now());

        when(rideService.acceptRide(rideId, driverId)).thenReturn(testRide);

        // Act & Assert
        mockMvc.perform(put("/api/rides/{rideId}/accept", rideId)
                        .param("driverId", String.valueOf(driverId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("ACCEPTED"))
                .andExpect(jsonPath("$.driver.id").value(2));

        verify(rideService, times(1)).acceptRide(rideId, driverId);
    }

    @Test
    void acceptRide_ShouldReturnBadRequest_WhenDriverIdMissing() throws Exception {
        // Arrange
        Long rideId = 1L;

        // Act & Assert
        mockMvc.perform(put("/api/rides/{rideId}/accept", rideId))
                .andExpect(status().isBadRequest());

        verify(rideService, never()).acceptRide(anyLong(), anyLong());
    }

    @Test
    void startRide_ShouldStartRide_WhenValidRideId() throws Exception {
        // Arrange
        Long rideId = 1L;
        testRide.setStatus(RideStatus.IN_PROGRESS);
        testRide.setRequestedAt(LocalDateTime.now());

        when(rideService.startRide(rideId)).thenReturn(testRide);

        // Act & Assert
        mockMvc.perform(put("/api/rides/{rideId}/start", rideId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"));

        verify(rideService, times(1)).startRide(rideId);
    }

    @Test
    void completeRide_ShouldCompleteRide_WhenValidRideId() throws Exception {
        // Arrange
        Long rideId = 1L;
        testRide.setStatus(RideStatus.COMPLETED);
        testRide.setCompletedAt(LocalDateTime.now());

        when(rideService.completeRide(rideId)).thenReturn(testRide);

        // Act & Assert
        mockMvc.perform(put("/api/rides/{rideId}/complete", rideId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("COMPLETED"));

        verify(rideService, times(1)).completeRide(rideId);
    }

    @Test
    void cancelRide_ShouldCancelRide_WithReason() throws Exception {
        // Arrange
        Long rideId = 1L;
        String reason = "Customer changed plans";
        testRide.setStatus(RideStatus.CANCELLED);

        when(rideService.cancelRide(rideId, reason)).thenReturn(testRide);

        // Act & Assert
        mockMvc.perform(put("/api/rides/{rideId}/cancel", rideId)
                        .param("reason", reason))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("CANCELLED"))
                .andExpect(jsonPath("$.cancellationReason").value(reason));

        verify(rideService, times(1)).cancelRide(rideId, reason);
    }

    @Test
    void cancelRide_ShouldCancelRide_WithoutReason() throws Exception {
        // Arrange
        Long rideId = 1L;
        testRide.setStatus(RideStatus.CANCELLED);

        when(rideService.cancelRide(rideId, null)).thenReturn(testRide);

        // Act & Assert
        mockMvc.perform(put("/api/rides/{rideId}/cancel", rideId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("CANCELLED"));

        verify(rideService, times(1)).cancelRide(rideId, null);
    }

    @Test
    void getCustomerRides_ShouldReturnRideList_WhenCustomerHasRides() throws Exception {
        // Arrange
        Long customerId = 1L;
        Ride ride2 = Ride.builder()
                .id(2L)
                .customer(testCustomer)
                .status(RideStatus.COMPLETED)
                .build();


        List<Ride> rides = Arrays.asList(testRide, ride2);
        when(rideService.getCustomerRides(customerId)).thenReturn(rides);

        // Act & Assert
        mockMvc.perform(get("/api/rides/customer/{customerId}", customerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[1].id").value(2));

        verify(rideService, times(1)).getCustomerRides(customerId);
    }

    @Test
    void getCustomerRides_ShouldReturnEmptyList_WhenNoRides() throws Exception {
        // Arrange
        Long customerId = 1L;
        when(rideService.getCustomerRides(customerId)).thenReturn(Collections.emptyList());

        // Act & Assert
        mockMvc.perform(get("/api/rides/customer/{customerId}", customerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        verify(rideService, times(1)).getCustomerRides(customerId);
    }

    @Test
    void getDriverRides_ShouldReturnRideList_WhenDriverHasRides() throws Exception {
        // Arrange
        Long driverId = 2L;
        Ride ride2 = Ride.builder()
                .id(2L)
                .driver(testDriver)
                .status(RideStatus.COMPLETED)
                .build();


        List<Ride> rides = Arrays.asList(testRide, ride2);
        when(rideService.getDriverRides(driverId)).thenReturn(rides);

        // Act & Assert
        mockMvc.perform(get("/api/rides/driver/{driverId}", driverId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[1].id").value(2));

        verify(rideService, times(1)).getDriverRides(driverId);
    }

    @Test
    void getDriverRides_ShouldReturnEmptyList_WhenNoRides() throws Exception {
        // Arrange
        Long driverId = 2L;
        when(rideService.getDriverRides(driverId)).thenReturn(Collections.emptyList());

        // Act & Assert
        mockMvc.perform(get("/api/rides/driver/{driverId}", driverId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        verify(rideService, times(1)).getDriverRides(driverId);
    }

    @Test
    void getAllRides_ShouldReturnAllRides() throws Exception {
        // Arrange
        Ride ride2 = Ride.builder()
                .id(2L)
                .status(RideStatus.COMPLETED)
                .build();

        Ride ride3 = Ride.builder()
                .id(3L)
                .status(RideStatus.IN_PROGRESS)
                .build();

        List<Ride> rides = Arrays.asList(testRide, ride2, ride3);
        when(rideService.getAllRides()).thenReturn(rides);

        // Act & Assert
        mockMvc.perform(get("/api/rides"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[2].id").value(3));

        verify(rideService, times(1)).getAllRides();
    }

    @Test
    void getAllRides_ShouldReturnEmptyList_WhenNoRides() throws Exception {
        // Arrange
        when(rideService.getAllRides()).thenReturn(Collections.emptyList());

        // Act & Assert
        mockMvc.perform(get("/api/rides"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        verify(rideService, times(1)).getAllRides();
    }

    @Test
    void requestRide_ShouldHandleServiceException() throws Exception {
        // Arrange
        Long customerId = 1L;
        when(rideService.requestRide(eq(customerId), any(RideRequest.class)))
                .thenThrow(new RuntimeException("No drivers available"));

        // Act & Assert
        mockMvc.perform(post("/api/rides/request")
                        .param("customerId", String.valueOf(customerId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testRideRequest)))
                .andExpect(status().is5xxServerError());

        verify(rideService, times(1)).requestRide(eq(customerId), any(RideRequest.class));
    }

    @Test
    void acceptRide_ShouldHandleInvalidRideId() throws Exception {
        // Arrange
        Long rideId = 999L;
        Long driverId = 2L;
        when(rideService.acceptRide(rideId, driverId))
                .thenThrow(new RuntimeException("Ride not found"));

        // Act & Assert
        mockMvc.perform(put("/api/rides/{rideId}/accept", rideId)
                        .param("driverId", String.valueOf(driverId)))
                .andExpect(status().is5xxServerError());

        verify(rideService, times(1)).acceptRide(rideId, driverId);
    }

    @Test
    void completeRide_ShouldHandleInvalidStateTransition() throws Exception {
        // Arrange
        Long rideId = 1L;
        when(rideService.completeRide(rideId))
                .thenThrow(new RuntimeException("Ride must be in progress"));

        // Act & Assert
        mockMvc.perform(put("/api/rides/{rideId}/complete", rideId))
                .andExpect(status().is5xxServerError());

        verify(rideService, times(1)).completeRide(rideId);
    }
}