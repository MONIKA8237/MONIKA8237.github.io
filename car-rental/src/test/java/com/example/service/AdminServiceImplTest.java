package com.example.service;

import com.example.models.Car;
import com.example.models.Customer;
import com.example.models.Reservation;
import com.example.repository.CarRepository;
import com.example.repository.CustomerRepository;
import com.example.repository.ReservationRepository;
import com.example.service.impl.AdminServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminServiceImplTest {

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private CarRepository carRepository;

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private AdminServiceImpl adminService;

    private Car testCar;
    private Customer testCustomer;
    private Reservation testReservation;

    @BeforeEach
    void setUp() {
        // Setup test car
        testCar = new Car();
        testCar.setId("car1");
        testCar.setModel("Toyota Camry");
        testCar.setBrand("Toyota");
        testCar.setCarStatus(Car.CarStatus.AVAILABLE);
        testCar.setCarType(Car.CarType.SEDAN);
        testCar.setRatePerHour(50.0);

        // Setup test customer
        testCustomer = new Customer();
        testCustomer.setId("customer1");
        testCustomer.setName("John Doe");
        testCustomer.setEmail("john@example.com");

        // Setup test reservation
        testReservation = new Reservation();
        testReservation.setReservationId("res1");
        testReservation.setCustomer(testCustomer);
        testReservation.setCar(testCar);
        testReservation.setStartTime("2023-10-01T10:00:00");
        testReservation.setEndTime("2023-10-05T10:00:00");
        testReservation.setStatus(Reservation.ReservationStatus.CONFIRMED);
        testReservation.setTotalCost(200.0);
    }

    @Test
    @DisplayName("Should add car successfully")
    void testAddCar_Success() {
        // Given
        when(carRepository.save(testCar)).thenReturn(testCar);

        // When
        adminService.addCar(testCar);

        // Then
        verify(carRepository, times(1)).save(testCar);
    }

    @Test
    @DisplayName("Should add car with all required fields")
    void testAddCar_WithAllFields() {
        // Given
        Car carWithAllFields = new Car();
        carWithAllFields.setId("car2");
        carWithAllFields.setModel("Honda Civic");
        carWithAllFields.setBrand("Honda");
        carWithAllFields.setCarStatus(Car.CarStatus.AVAILABLE);

        when(carRepository.save(carWithAllFields)).thenReturn(carWithAllFields);

        // When
        adminService.addCar(carWithAllFields);

        // Then
        ArgumentCaptor<Car> carCaptor = ArgumentCaptor.forClass(Car.class);
        verify(carRepository, times(1)).save(carCaptor.capture());

        Car savedCar = carCaptor.getValue();
        assertEquals("car2", savedCar.getId());
        assertEquals("Honda Civic", savedCar.getModel());
        assertEquals("Honda", savedCar.getBrand());
        assertEquals(Car.CarStatus.AVAILABLE, savedCar.getCarStatus());
    }

    @Test
    @DisplayName("Should remove car successfully when no active reservations")
    void testRemoveCar_Success_NoActiveReservations() {
        // Given - Car with only cancelled reservations
        Reservation cancelledReservation = new Reservation();
        cancelledReservation.setReservationId("res1");
        cancelledReservation.setStatus(Reservation.ReservationStatus.CANCELLED);

        when(reservationRepository.findByCarId(testCar.getId()))
                .thenReturn(Arrays.asList(cancelledReservation));

        // When
        adminService.removeCar(testCar);

        // Then
        verify(reservationRepository, times(1)).findByCarId(testCar.getId());
        verify(carRepository, times(1)).delete(testCar);
    }

    @Test
    @DisplayName("Should remove car successfully when no reservations at all")
    void testRemoveCar_Success_NoReservations() {
        // Given - Car with no reservations
        when(reservationRepository.findByCarId(testCar.getId()))
                .thenReturn(Collections.emptyList());

        // When
        adminService.removeCar(testCar);

        // Then
        verify(reservationRepository, times(1)).findByCarId(testCar.getId());
        verify(carRepository, times(1)).delete(testCar);
    }

    @Test
    @DisplayName("Should throw exception when trying to remove car with active reservations")
    void testRemoveCar_ThrowsException_HasActiveReservations() {
        // Given - Car with active reservations
        Reservation activeReservation = new Reservation();
        activeReservation.setReservationId("res1");
        activeReservation.setStatus(Reservation.ReservationStatus.CONFIRMED);

        when(reservationRepository.findByCarId(testCar.getId()))
                .thenReturn(Arrays.asList(activeReservation));

        // When & Then
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            adminService.removeCar(testCar);
        });

        assertEquals("Cannot remove car with active reservations", exception.getMessage());
        verify(reservationRepository, times(1)).findByCarId(testCar.getId());
        verify(carRepository, never()).delete(any(Car.class));
    }

    @Test
    @DisplayName("Should throw exception when car has multiple active reservations")
    void testRemoveCar_ThrowsException_MultipleActiveReservations() {
        // Given - Car with multiple active reservations
        Reservation activeReservation1 = new Reservation();
        activeReservation1.setStatus(Reservation.ReservationStatus.CONFIRMED);

        Reservation activeReservation2 = new Reservation();
        activeReservation2.setStatus(Reservation.ReservationStatus.PENDING);

        Reservation cancelledReservation = new Reservation();
        cancelledReservation.setStatus(Reservation.ReservationStatus.CANCELLED);

        when(reservationRepository.findByCarId(testCar.getId()))
                .thenReturn(Arrays.asList(activeReservation1, activeReservation2, cancelledReservation));

        // When & Then
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            adminService.removeCar(testCar);
        });

        assertEquals("Cannot remove car with active reservations", exception.getMessage());
        verify(carRepository, never()).delete(any(Car.class));
    }

    @Test
    @DisplayName("Should update car details successfully")
    void testUpdateCarDetails_Success() {
        // Given
        testCar.setModel("Updated Toyota Camry");
        when(carRepository.save(testCar)).thenReturn(testCar);

        // When
        adminService.updateCarDetails(testCar);

        // Then
        ArgumentCaptor<Car> carCaptor = ArgumentCaptor.forClass(Car.class);
        verify(carRepository, times(1)).save(carCaptor.capture());

        Car updatedCar = carCaptor.getValue();
        assertEquals("Updated Toyota Camry", updatedCar.getModel());
    }

    @Test
    @DisplayName("Should view all cars when cars exist")
    void testViewAllCars_Success_CarsExist() {
        // Given
        Car car2 = new Car();
        car2.setId("car2");
        car2.setModel("Honda Civic");
        car2.setCarType(Car.CarType.SEDAN);
        car2.setCarStatus(Car.CarStatus.RENTED);
        car2.setRatePerHour(45.0);

        List<Car> cars = Arrays.asList(testCar, car2);
        when(carRepository.findAll()).thenReturn(cars);

        // When
        List<Car> result = adminService.viewAllCars();

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(testCar, result.get(0));
        assertEquals(car2, result.get(1));
        verify(carRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Should view all cars when no cars exist")
    void testViewAllCars_Success_NoCarsExist() {
        // Given
        when(carRepository.findAll()).thenReturn(Collections.emptyList());

        // When
        List<Car> result = adminService.viewAllCars();

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(carRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Should view all reservations when reservations exist")
    void testViewAllReservations_Success_ReservationsExist() {
        // Given
        Reservation reservation2 = new Reservation();
        reservation2.setReservationId("res2");
        reservation2.setCustomer(testCustomer);
        reservation2.setCar(testCar);
        reservation2.setStatus(Reservation.ReservationStatus.PENDING);
        reservation2.setTotalCost(150.0);

        List<Reservation> reservations = Arrays.asList(testReservation, reservation2);
        when(reservationRepository.findAll()).thenReturn(reservations);

        // When
        List<Reservation> result = adminService.viewAllReservations();

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(testReservation, result.get(0));
        assertEquals(reservation2, result.get(1));
        verify(reservationRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Should view all reservations when no reservations exist")
    void testViewAllReservations_Success_NoReservationsExist() {
        // Given
        when(reservationRepository.findAll()).thenReturn(Collections.emptyList());

        // When
        List<Reservation> result = adminService.viewAllReservations();

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(reservationRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Should make reservation for customer successfully")
    void testMakeReservationForCustomer_Success() {
        // Given
        String startTime = "2023-10-01T10:00:00";
        String endTime = "2023-10-05T10:00:00";

        when(reservationRepository.save(any(Reservation.class))).thenAnswer(invocation -> {
            Reservation reservation = invocation.getArgument(0);
            reservation.setReservationId("RES123");
            return reservation;
        });
        when(customerRepository.save(testCustomer)).thenReturn(testCustomer);
        when(carRepository.save(testCar)).thenReturn(testCar);

        // When
        adminService.makeReservationForCustomer(testCustomer, testCar, startTime, endTime);

        // Then
        ArgumentCaptor<Reservation> reservationCaptor = ArgumentCaptor.forClass(Reservation.class);
        verify(reservationRepository, times(1)).save(reservationCaptor.capture());
        verify(customerRepository, times(1)).save(testCustomer);
        verify(carRepository, times(1)).save(testCar);

        Reservation savedReservation = reservationCaptor.getValue();
        assertEquals(testCustomer, savedReservation.getCustomer());
        assertEquals(testCar, savedReservation.getCar());
        assertEquals(startTime, savedReservation.getStartTime());
        assertEquals(endTime, savedReservation.getEndTime());
        assertEquals(Reservation.ReservationStatus.CONFIRMED, savedReservation.getStatus());
    }

    @Test
    @DisplayName("Should not make reservation when car is not available")
    void testMakeReservationForCustomer_CarNotAvailable() {
        // Given
        testCar.setCarStatus(Car.CarStatus.RENTED);
        String startTime = "2023-10-01T10:00:00";
        String endTime = "2023-10-05T10:00:00";

        // When
        adminService.makeReservationForCustomer(testCustomer, testCar, startTime, endTime);

        // Then
        verify(reservationRepository, never()).save(any(Reservation.class));
        verify(customerRepository, never()).save(any(Customer.class));
        verify(carRepository, never()).save(any(Car.class));
    }

    @Test
    @DisplayName("Should not make reservation when car is in maintenance")
    void testMakeReservationForCustomer_CarInMaintenance() {
        // Given
        testCar.setCarStatus(Car.CarStatus.MAINTENANCE);
        String startTime = "2023-10-01T10:00:00";
        String endTime = "2023-10-05T10:00:00";

        // When
        adminService.makeReservationForCustomer(testCustomer, testCar, startTime, endTime);

        // Then
        verify(reservationRepository, never()).save(any(Reservation.class));
        verify(customerRepository, never()).save(any(Customer.class));
        verify(carRepository, never()).save(any(Car.class));
    }

    @Test
    @DisplayName("Should handle null car gracefully")
    void testMakeReservationForCustomer_NullCar() {
        // Given
        String startTime = "2023-10-01T10:00:00";
        String endTime = "2023-10-05T10:00:00";

        // When & Then
        assertThrows(NullPointerException.class, () -> {
            adminService.makeReservationForCustomer(testCustomer, null, startTime, endTime);
        });

        verify(reservationRepository, never()).save(any(Reservation.class));
    }

    @Test
    @DisplayName("Should cancel reservation successfully")
    void testCancelReservation_Success() {
        // Given
        String reservationId = "res1";
        when(reservationRepository.findById(reservationId)).thenReturn(Optional.of(testReservation));
        when(reservationRepository.save(testReservation)).thenReturn(testReservation);
        when(carRepository.save(testCar)).thenReturn(testCar);

        // When
        adminService.cancelReservation(reservationId);

        // Then
        verify(reservationRepository, times(1)).findById(reservationId);
        verify(reservationRepository, times(1)).save(testReservation);
        verify(carRepository, times(1)).save(testCar);

        // Verify reservation was cancelled
        assertEquals(Reservation.ReservationStatus.CANCELLED, testReservation.getStatus());
    }

    @Test
    @DisplayName("Should throw exception when reservation not found")
    void testCancelReservation_ReservationNotFound() {
        // Given
        String reservationId = "nonexistent";
        when(reservationRepository.findById(reservationId)).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            adminService.cancelReservation(reservationId);
        });

        assertEquals("Reservation not found", exception.getMessage());
        verify(reservationRepository, times(1)).findById(reservationId);
        verify(reservationRepository, never()).save(any(Reservation.class));
        verify(carRepository, never()).save(any(Car.class));
    }

    @Test
    @DisplayName("Should handle empty reservation ID")
    void testCancelReservation_EmptyReservationId() {
        // Given
        String emptyReservationId = "";
        when(reservationRepository.findById(emptyReservationId)).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            adminService.cancelReservation(emptyReservationId);
        });

        assertEquals("Reservation not found", exception.getMessage());
        verify(reservationRepository, times(1)).findById(emptyReservationId);
    }

    @Test
    @DisplayName("Should handle null reservation ID")
    void testCancelReservation_NullReservationId() {
        // Given
        when(reservationRepository.findById(null)).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            adminService.cancelReservation(null);
        });

        assertEquals("Reservation not found", exception.getMessage());
        verify(reservationRepository, times(1)).findById(null);
    }

    @Test
    @DisplayName("Should save reservation with correct save order")
    void testMakeReservationForCustomer_SaveOrder() {
        // Given
        String startTime = "2023-10-01T10:00:00";
        String endTime = "2023-10-05T10:00:00";

        when(reservationRepository.save(any(Reservation.class))).thenReturn(testReservation);
        when(customerRepository.save(testCustomer)).thenReturn(testCustomer);
        when(carRepository.save(testCar)).thenReturn(testCar);

        // When
        adminService.makeReservationForCustomer(testCustomer, testCar, startTime, endTime);

        // Then - Verify save order using InOrder
        InOrder inOrder = inOrder(reservationRepository, customerRepository, carRepository);
        inOrder.verify(reservationRepository).save(any(Reservation.class));
        inOrder.verify(customerRepository).save(testCustomer);
        inOrder.verify(carRepository).save(testCar);
    }

    @Test
    @DisplayName("Should handle repository save failures")
    void testMakeReservationForCustomer_RepositorySaveFailure() {
        // Given
        String startTime = "2023-10-01T10:00:00";
        String endTime = "2023-10-05T10:00:00";

        when(reservationRepository.save(any(Reservation.class)))
                .thenThrow(new RuntimeException("Database error"));

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            adminService.makeReservationForCustomer(testCustomer, testCar, startTime, endTime);
        });

        // Verify subsequent saves are not called due to exception
        verify(customerRepository, never()).save(any(Customer.class));
        verify(carRepository, never()).save(any(Car.class));
    }

}