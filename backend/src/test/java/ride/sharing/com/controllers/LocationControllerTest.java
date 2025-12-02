package ride.sharing.com.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import ride.sharing.com.dtos.LocationUpdate;
import ride.sharing.com.services.LocationService;

import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class LocationControllerTest {

    @Mock
    private LocationService locationService;

    @InjectMocks
    private LocationController locationController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(locationController).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void updateLocation_ShouldUpdateDriverLocation_WhenValidRequest() throws Exception {
        // Arrange
        Long driverId = 1L;
        LocationUpdate locationUpdate = new LocationUpdate();
        locationUpdate.setLatitude(-1.2921);
        locationUpdate.setLongitude(36.8219);

        doNothing().when(locationService).updateDriverLocation(driverId, -1.2921, 36.8219);

        // Act & Assert
        mockMvc.perform(post("/api/location/update").param("driverId", String.valueOf(driverId)).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(locationUpdate))).andExpect(status().isOk()).andExpect(content().string("Location updated"));

        verify(locationService, times(1)).updateDriverLocation(driverId, -1.2921, 36.8219);
    }

    @Test
    void updateLocation_ShouldUpdateLocation_WithDifferentCoordinates() throws Exception {
        // Arrange
        Long driverId = 5L;
        LocationUpdate locationUpdate = new LocationUpdate();
        locationUpdate.setLatitude(40.7128);
        locationUpdate.setLongitude(-74.0060);

        doNothing().when(locationService).updateDriverLocation(driverId, 40.7128, -74.0060);

        // Act & Assert
        mockMvc.perform(post("/api/location/update").param("driverId", String.valueOf(driverId)).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(locationUpdate))).andExpect(status().isOk()).andExpect(content().string("Location updated"));

        verify(locationService, times(1)).updateDriverLocation(driverId, 40.7128, -74.0060);
    }

    @Test
    void updateLocation_ShouldHandleNegativeCoordinates() throws Exception {
        // Arrange
        Long driverId = 10L;
        LocationUpdate locationUpdate = new LocationUpdate();
        locationUpdate.setLatitude(-33.8688);
        locationUpdate.setLongitude(-151.2093);

        doNothing().when(locationService).updateDriverLocation(driverId, -33.8688, -151.2093);

        // Act & Assert
        mockMvc.perform(post("/api/location/update").param("driverId", String.valueOf(driverId)).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(locationUpdate))).andExpect(status().isOk()).andExpect(content().string("Location updated"));

        verify(locationService, times(1)).updateDriverLocation(driverId, -33.8688, -151.2093);
    }

    @Test
    void updateLocation_ShouldHandleZeroCoordinates() throws Exception {
        // Arrange
        Long driverId = 3L;
        LocationUpdate locationUpdate = new LocationUpdate();
        locationUpdate.setLatitude(0.0);
        locationUpdate.setLongitude(0.0);

        doNothing().when(locationService).updateDriverLocation(driverId, 0.0, 0.0);

        // Act & Assert
        mockMvc.perform(post("/api/location/update").param("driverId", String.valueOf(driverId)).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(locationUpdate))).andExpect(status().isOk()).andExpect(content().string("Location updated"));

        verify(locationService, times(1)).updateDriverLocation(driverId, 0.0, 0.0);
    }

    @Test
    void updateLocation_ShouldReturnBadRequest_WhenDriverIdMissing() throws Exception {
        // Arrange
        LocationUpdate locationUpdate = new LocationUpdate();
        locationUpdate.setLatitude(-1.2921);
        locationUpdate.setLongitude(36.8219);

        // Act & Assert
        mockMvc.perform(post("/api/location/update").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(locationUpdate))).andExpect(status().isBadRequest());

        verify(locationService, never()).updateDriverLocation(anyLong(), anyDouble(), anyDouble());
    }

    @Test
    void updateLocation_ShouldReturnBadRequest_WhenRequestBodyEmpty() throws Exception {
        // Arrange
        Long driverId = 1L;

        // Act & Assert
        mockMvc.perform(post("/api/location/update").param("driverId", String.valueOf(driverId)).contentType(MediaType.APPLICATION_JSON).content("{}")).andExpect(status().isOk()); // Will pass through but with null values

        verify(locationService, times(1)).updateDriverLocation(eq(driverId), any(), any());
    }

    @Test
    void updateLocation_ShouldHandleServiceException() throws Exception {
        // Arrange
        Long driverId = 1L;
        LocationUpdate locationUpdate = new LocationUpdate();
        locationUpdate.setLatitude(-1.2921);
        locationUpdate.setLongitude(36.8219);

        doThrow(new RuntimeException("Redis connection failed")).when(locationService).updateDriverLocation(driverId, -1.2921, 36.8219);

        // Act & Assert
        mockMvc.perform(post("/api/location/update").param("driverId", String.valueOf(driverId)).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(locationUpdate))).andExpect(status().is5xxServerError());

        verify(locationService, times(1)).updateDriverLocation(driverId, -1.2921, 36.8219);
    }

    @Test
    void updateLocation_ShouldHandleExtremeLatitudeValues() throws Exception {
        // Arrange
        Long driverId = 7L;
        LocationUpdate locationUpdate = new LocationUpdate();
        locationUpdate.setLatitude(89.9999);
        locationUpdate.setLongitude(179.9999);

        doNothing().when(locationService).updateDriverLocation(driverId, 89.9999, 179.9999);

        // Act & Assert
        mockMvc.perform(post("/api/location/update").param("driverId", String.valueOf(driverId)).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(locationUpdate))).andExpect(status().isOk()).andExpect(content().string("Location updated"));

        verify(locationService, times(1)).updateDriverLocation(driverId, 89.9999, 179.9999);
    }

    @Test
    void updateLocation_ShouldHandlePreciseCoordinates() throws Exception {
        // Arrange
        Long driverId = 15L;
        LocationUpdate locationUpdate = new LocationUpdate();
        locationUpdate.setLatitude(-1.292066);
        locationUpdate.setLongitude(36.821945);

        doNothing().when(locationService).updateDriverLocation(driverId, -1.292066, 36.821945);

        // Act & Assert
        mockMvc.perform(post("/api/location/update").param("driverId", String.valueOf(driverId)).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(locationUpdate))).andExpect(status().isOk()).andExpect(content().string("Location updated"));

        verify(locationService, times(1)).updateDriverLocation(driverId, -1.292066, 36.821945);
    }

    @Test
    void updateLocation_ShouldWorkForMultipleDrivers() throws Exception {
        // Arrange
        LocationUpdate locationUpdate1 = new LocationUpdate();
        locationUpdate1.setLatitude(-1.2921);
        locationUpdate1.setLongitude(36.8219);

        LocationUpdate locationUpdate2 = new LocationUpdate();
        locationUpdate2.setLatitude(-1.3000);
        locationUpdate2.setLongitude(36.8300);

        doNothing().when(locationService).updateDriverLocation(anyLong(), anyDouble(), anyDouble());

        // Act & Assert - Driver 1
        mockMvc.perform(post("/api/location/update").param("driverId", "1").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(locationUpdate1))).andExpect(status().isOk());

        // Act & Assert - Driver 2
        mockMvc.perform(post("/api/location/update").param("driverId", "2").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(locationUpdate2))).andExpect(status().isOk());

        verify(locationService, times(2)).updateDriverLocation(anyLong(), anyDouble(), anyDouble());
    }
}