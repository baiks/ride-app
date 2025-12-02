package ride.sharing.com.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import ride.sharing.com.dtos.RideRequest;
import ride.sharing.com.enums.DriverStatus;
import ride.sharing.com.enums.RideStatus;
import ride.sharing.com.enums.Role;
import ride.sharing.com.exception.DriverNotAvailableException;
import ride.sharing.com.exception.ForbiddenException;
import ride.sharing.com.exception.RideNotAvailableException;
import ride.sharing.com.impl.RideServiceImpl;
import ride.sharing.com.models.Ride;
import ride.sharing.com.models.User;
import ride.sharing.com.repositories.RideRepository;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RideServiceImplTest {

    @Mock
    private RideRepository rideRepository;

    @Mock
    private UserService userService;

    @Mock
    private MatchingService matchingService;

    @Mock
    private LocationService locationService;

    @InjectMocks
    private RideServiceImpl rideService;

    private User customer;
    private User driver;
    private Ride ride;
    private RideRequest rideRequest;

    @BeforeEach
    void setup() {
        customer = User.builder()
                .id(1L)
                .role(Role.CUSTOMER)
                .build();

        driver = User.builder()
                .id(2L)
                .role(Role.DRIVER)
                .driverStatus(DriverStatus.AVAILABLE)
                .build();

        ride = Ride.builder()
                .id(10L)
                .customer(customer)
                .driver(driver)
                .status(RideStatus.REQUESTED)
                .build();

        rideRequest = new RideRequest();
        rideRequest.setPickupLat(1.0);
        rideRequest.setPickupLng(1.0);
        rideRequest.setPickupAddress("Pickup Address");
        rideRequest.setDropoffLat(2.0);
        rideRequest.setDropoffLng(2.0);
        rideRequest.setDropoffAddress("Dropoff Address");
    }

    @Test
    void requestRide_shouldCreateRideAndAssignDriver() {
        when(userService.findById(customer.getId())).thenReturn(customer);
        when(matchingService.findNearestDriver(1.0, 1.0)).thenReturn(driver.getId());
        when(userService.findById(driver.getId())).thenReturn(driver);
        when(rideRepository.save(any(Ride.class))).thenAnswer(i -> i.getArguments()[0]);

        Ride createdRide = rideService.requestRide(customer.getId(), rideRequest);

        assertThat(createdRide.getCustomer()).isEqualTo(customer);
        assertThat(createdRide.getDriver()).isEqualTo(driver);
        assertThat(createdRide.getStatus()).isEqualTo(RideStatus.REQUESTED);
        assertThat(createdRide.getDistance()).isGreaterThan(0);
        assertThat(createdRide.getFare()).isGreaterThan(0);
        verify(rideRepository).save(any(Ride.class));
    }

    @Test
    void requestRide_whenUserNotCustomer_shouldThrow() {
        User notCustomer = User.builder().id(3L).role(Role.DRIVER).build();
        when(userService.findById(notCustomer.getId())).thenReturn(notCustomer);

        assertThatThrownBy(() -> rideService.requestRide(notCustomer.getId(), rideRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Only customers can request rides");
    }

    @Test
    void requestRide_whenMatchingServiceFails_shouldCreateRideWithoutDriver() {
        when(userService.findById(customer.getId())).thenReturn(customer);
        when(matchingService.findNearestDriver(anyDouble(), anyDouble())).thenThrow(new RuntimeException("No drivers"));
        when(rideRepository.save(any(Ride.class))).thenAnswer(i -> i.getArguments()[0]);

        Ride createdRide = rideService.requestRide(customer.getId(), rideRequest);

        assertThat(createdRide.getDriver()).isNull();
        assertThat(createdRide.getStatus()).isEqualTo(RideStatus.REQUESTED);
    }

    @Test
    void acceptRide_shouldAcceptRideAndSetDriverStatusBusy() {
        ride.setStatus(RideStatus.REQUESTED);
        when(rideRepository.findById(ride.getId())).thenReturn(Optional.of(ride));
        when(userService.findById(driver.getId())).thenReturn(driver);
        when(rideRepository.save(any(Ride.class))).thenAnswer(i -> i.getArguments()[0]);
        when(userService.updateDriverStatus(driver.getId(), DriverStatus.BUSY)).thenReturn(driver);

        Ride acceptedRide = rideService.acceptRide(ride.getId(), driver.getId());

        assertThat(acceptedRide.getStatus()).isEqualTo(RideStatus.ACCEPTED);
        assertThat(acceptedRide.getDriver()).isEqualTo(driver);
        verify(userService).updateDriverStatus(driver.getId(), DriverStatus.BUSY);
    }

    @Test
    void acceptRide_whenRideNotRequested_shouldThrow() {
        ride.setStatus(RideStatus.ACCEPTED);
        when(rideRepository.findById(ride.getId())).thenReturn(Optional.of(ride));

        assertThatThrownBy(() -> rideService.acceptRide(ride.getId(), driver.getId()))
                .isInstanceOf(RideNotAvailableException.class)
                .hasMessage("Ride is not available for acceptance");
    }

    @Test
    void acceptRide_whenUserNotDriver_shouldThrow() {
        ride.setStatus(RideStatus.REQUESTED);
        User notDriver = User.builder().id(5L).role(Role.CUSTOMER).build();
        when(rideRepository.findById(ride.getId())).thenReturn(Optional.of(ride));
        when(userService.findById(notDriver.getId())).thenReturn(notDriver);

        assertThatThrownBy(() -> rideService.acceptRide(ride.getId(), notDriver.getId()))
                .isInstanceOf(ForbiddenException.class)
                .hasMessage("Only drivers can accept rides");
    }

    @Test
    void acceptRide_whenDriverNotAvailable_shouldThrow() {
        ride.setStatus(RideStatus.REQUESTED);
        driver.setDriverStatus(DriverStatus.OFFLINE);
        when(rideRepository.findById(ride.getId())).thenReturn(Optional.of(ride));
        when(userService.findById(driver.getId())).thenReturn(driver);

        assertThatThrownBy(() -> rideService.acceptRide(ride.getId(), driver.getId()))
                .isInstanceOf(DriverNotAvailableException.class)
                .hasMessage("Driver is not available");
    }

    @Test
    void startRide_shouldStartRideIfAccepted() {
        ride.setStatus(RideStatus.ACCEPTED);
        when(rideRepository.findById(ride.getId())).thenReturn(Optional.of(ride));
        when(rideRepository.save(any(Ride.class))).thenAnswer(i -> i.getArguments()[0]);

        Ride startedRide = rideService.startRide(ride.getId());

        assertThat(startedRide.getStatus()).isEqualTo(RideStatus.IN_PROGRESS);
    }

    @Test
    void startRide_whenNotAccepted_shouldThrow() {
        ride.setStatus(RideStatus.REQUESTED);
        when(rideRepository.findById(ride.getId())).thenReturn(Optional.of(ride));

        assertThatThrownBy(() -> rideService.startRide(ride.getId()))
                .isInstanceOf(ForbiddenException.class)
                .hasMessage("Ride must be accepted before starting");
    }

    @Test
    void completeRide_shouldCompleteRideAndUpdateDriverStatus() {
        ride.setStatus(RideStatus.IN_PROGRESS);
        when(rideRepository.findById(ride.getId())).thenReturn(Optional.of(ride));
        when(rideRepository.save(any(Ride.class))).thenAnswer(i -> i.getArguments()[0]);
        when(userService.updateDriverStatus(driver.getId(), DriverStatus.AVAILABLE)).thenReturn(driver);
        doNothing().when(locationService).updateDriverLocation(driver.getId(), ride.getDropoffLat(), ride.getDropoffLng());

        Ride completedRide = rideService.completeRide(ride.getId());

        assertThat(completedRide.getStatus()).isEqualTo(RideStatus.COMPLETED);
        verify(userService).updateDriverStatus(driver.getId(), DriverStatus.AVAILABLE);
        verify(locationService).updateDriverLocation(driver.getId(), ride.getDropoffLat(), ride.getDropoffLng());
    }

    @Test
    void completeRide_whenInvalidStatus_shouldThrow() {
        ride.setStatus(RideStatus.CANCELLED);
        when(rideRepository.findById(ride.getId())).thenReturn(Optional.of(ride));

        assertThatThrownBy(() -> rideService.completeRide(ride.getId()))
                .isInstanceOf(ForbiddenException.class)
                .hasMessage("Cannot complete ride in current status");
    }

    @Test
    void cancelRide_shouldCancelAndSetDriverAvailable() {
        ride.setStatus(RideStatus.REQUESTED);
        driver.setDriverStatus(DriverStatus.BUSY);
        when(rideRepository.findById(ride.getId())).thenReturn(Optional.of(ride));
        when(rideRepository.save(any(Ride.class))).thenAnswer(i -> i.getArguments()[0]);
        when(userService.updateDriverStatus(driver.getId(), DriverStatus.AVAILABLE)).thenReturn(driver);

        Ride cancelledRide = rideService.cancelRide(ride.getId(), "Customer requested");

        assertThat(cancelledRide.getStatus()).isEqualTo(RideStatus.CANCELLED);
        verify(userService).updateDriverStatus(driver.getId(), DriverStatus.AVAILABLE);
    }

    @Test
    void cancelRide_whenAlreadyCompleted_shouldThrow() {
        ride.setStatus(RideStatus.COMPLETED);
        when(rideRepository.findById(ride.getId())).thenReturn(Optional.of(ride));

        assertThatThrownBy(() -> rideService.cancelRide(ride.getId(), "Late cancellation"))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Cannot cancel a completed ride");
    }

    @Test
    void getCustomerRides_shouldReturnRides() {
        List<Ride> rides = List.of(ride);
        when(rideRepository.findByCustomerId(customer.getId())).thenReturn(rides);

        List<Ride> result = rideService.getCustomerRides(customer.getId());

        assertThat(result).isEqualTo(rides);
    }

    @Test
    void getDriverRides_shouldReturnRides() {
        List<Ride> rides = List.of(ride);
        when(rideRepository.findByDriverId(driver.getId())).thenReturn(rides);

        List<Ride> result = rideService.getDriverRides(driver.getId());

        assertThat(result).isEqualTo(rides);
    }

    @Test
    void getRideById_shouldReturnRide() {
        when(rideRepository.findById(ride.getId())).thenReturn(Optional.of(ride));

        Ride foundRide = rideService.getRideById(ride.getId());

        assertThat(foundRide).isEqualTo(ride);
    }

    @Test
    void getRideById_whenNotFound_shouldThrow() {
        when(rideRepository.findById(ride.getId())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> rideService.getRideById(ride.getId()))
                .isInstanceOf(RideNotAvailableException.class)
                .hasMessageContaining("Ride not found");
    }

    @Test
    void getPendingRides_shouldReturnRequestedRides() {
        List<Ride> rides = List.of(ride);
        when(rideRepository.findByStatus(RideStatus.REQUESTED)).thenReturn(rides);

        List<Ride> result = rideService.getPendingRides();

        assertThat(result).isEqualTo(rides);
    }

    @Test
    void getAllRides_shouldReturnAll() {
        List<Ride> rides = List.of(ride);
        when(rideRepository.findAll()).thenReturn(rides);

        List<Ride> result = rideService.getAllRides();

        assertThat(result).isEqualTo(rides);
    }
}
