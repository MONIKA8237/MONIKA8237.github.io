package com.example.service;

import com.example.models.Car;
import com.example.models.Customer;
import com.example.models.Reservation;
import com.example.repository.CarRepository;
import com.example.repository.CustomerRepository;
import com.example.repository.ReservationRepository;
import com.example.service.impl.CustomerServiceImpl;
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
class CustomerServiceImplTest {

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private CarRepository carRepository;

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private CustomerServiceImpl customerService;

    private Car availableCar;
    private Car rentedCar;
    private Car maintenanceCar;
    private Customer testCustomer;
    private Reservation testReservation;

    @BeforeEach
    void setUp() {
        // Setup available car
        availableCar = new Car();
        availableCar.setId("car1");
        availableCar.setModel("Toyota Camry");
        availableCar.setBrand("Toyota");
        availableCar.setCarStatus(Car.CarStatus.AVAILABLE);
        availableCar.setCarType(Car.CarType.SEDAN);
        availableCar.setRatePerHour(50.0);

        // Setup rented car
        rentedCar = new Car();
        rentedCar.setId("car2");
        rentedCar.setModel("Honda Civic");
        rentedCar.setBrand("Honda");
        rentedCar.setCarStatus(Car.CarStatus.RENTED);
        rentedCar.setCarType(Car.CarType.SEDAN);
        rentedCar.setRatePerHour(45.0);

        // Setup maintenance car
        maintenanceCar = new Car();
        maintenanceCar.setId("car3");
        maintenanceCar.setModel("BMW X5");
        maintenanceCar.setBrand("BMW");
        maintenanceCar.setCarStatus(Car.CarStatus.MAINTENANCE);
        maintenanceCar.setCarType(Car.CarType.SUV);
        maintenanceCar.setRatePerHour(80.0);

        // Setup test customer
        testCustomer = new Customer();
        testCustomer.setId("customer1");
        testCustomer.setName("John Doe");
        testCustomer.setEmail("john@example.com");
        testCustomer.setPhoneNumber("+1234567890");

        // Setup test reservation
        testReservation = new Reservation();
        testReservation.setReservationId("res1");
        testReservation.setCustomer(testCustomer);
        testReservation.setCar(availableCar);
        testReservation.setStartTime("2023-10-01T10:00:00");
        testReservation.setEndTime("2023-10-05T10:00:00");
        testReservation.setStatus(Reservation.ReservationStatus.CONFIRMED);
        testReservation.setTotalCost(200.0);
    }

    @Test
    @DisplayName("Should return available cars when cars exist")
    void testViewAvailableCars_Success_CarsAvailable() {
        // Given
        List<Car> allCars = Arrays.asList(availableCar, rentedCar, maintenanceCar);
        when(carRepository.findAll()).thenReturn(allCars);

        // When
        List<Car> result = customerService.viewAvailableCars();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(availableCar, result.get(0));
        assertEquals(Car.CarStatus.AVAILABLE, result.get(0).getCarStatus());
        verify(carRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Should return multiple available cars when multiple cars are available")
    void testViewAvailableCars_Success_MultipleCarsAvailable() {
        // Given
        Car anotherAvailableCar = new Car();
        anotherAvailableCar.setId("car4");
        anotherAvailableCar.setModel("Ford Focus");
        anotherAvailableCar.setCarStatus(Car.CarStatus.AVAILABLE);
        anotherAvailableCar.setCarType(Car.CarType.SUV);
        anotherAvailableCar.setRatePerHour(40.0);

        List<Car> allCars = Arrays.asList(availableCar, rentedCar, anotherAvailableCar, maintenanceCar);
        when(carRepository.findAll()).thenReturn(allCars);

        // When
        List<Car> result = customerService.viewAvailableCars();

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.contains(availableCar));
        assertTrue(result.contains(anotherAvailableCar));
        assertTrue(result.stream().allMatch(car -> car.getCarStatus() == Car.CarStatus.AVAILABLE));
        verify(carRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Should return empty list when no cars are available")
    void testViewAvailableCars_Success_NoCarsAvailable() {
        // Given - Only non-available cars
        List<Car> allCars = Arrays.asList(rentedCar, maintenanceCar);
        when(carRepository.findAll()).thenReturn(allCars);

        // When
        List<Car> result = customerService.viewAvailableCars();

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(carRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Should return empty list when no cars exist in database")
    void testViewAvailableCars_Success_NoCarsInDatabase() {
        // Given
        when(carRepository.findAll()).thenReturn(Collections.emptyList());

        // When
        List<Car> result = customerService.viewAvailableCars();

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(carRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Should make reservation successfully when car is available")
    void testMakeReservation_Success() {
        // Given
        String startTime = "2023-10-01T10:00:00";
        String endTime = "2023-10-05T10:00:00";

        when(reservationRepository.save(any(Reservation.class))).thenAnswer(invocation -> {
            Reservation reservation = invocation.getArgument(0);
            reservation.setReservationId("RES123");
            return reservation;
        });
        when(customerRepository.save(testCustomer)).thenReturn(testCustomer);
        when(carRepository.save(availableCar)).thenReturn(availableCar);

        // When
        customerService.makeReservation(testCustomer, availableCar, startTime, endTime);

        // Then
        ArgumentCaptor<Reservation> reservationCaptor = ArgumentCaptor.forClass(Reservation.class);
        verify(reservationRepository, times(1)).save(reservationCaptor.capture());
        verify(customerRepository, times(1)).save(testCustomer);
        verify(carRepository, times(1)).save(availableCar);

        Reservation savedReservation = reservationCaptor.getValue();
        assertEquals(testCustomer, savedReservation.getCustomer());
        assertEquals(availableCar, savedReservation.getCar());
        assertEquals(startTime, savedReservation.getStartTime());
        assertEquals(endTime, savedReservation.getEndTime());
        assertEquals(Reservation.ReservationStatus.CONFIRMED, savedReservation.getStatus());
    }

    @Test
    @DisplayName("Should not make reservation when car is rented")
    void testMakeReservation_CarNotAvailable_Rented() {
        // Given
        String startTime = "2023-10-01T10:00:00";
        String endTime = "2023-10-05T10:00:00";

        // When
        customerService.makeReservation(testCustomer, rentedCar, startTime, endTime);

        // Then
        verify(reservationRepository, never()).save(any(Reservation.class));
        verify(customerRepository, never()).save(any(Customer.class));
        verify(carRepository, never()).save(any(Car.class));
    }

    @Test
    @DisplayName("Should not make reservation when car is in maintenance")
    void testMakeReservation_CarNotAvailable_Maintenance() {
        // Given
        String startTime = "2023-10-01T10:00:00";
        String endTime = "2023-10-05T10:00:00";

        // When
        customerService.makeReservation(testCustomer, maintenanceCar, startTime, endTime);

        // Then
        verify(reservationRepository, never()).save(any(Reservation.class));
        verify(customerRepository, never()).save(any(Customer.class));
        verify(carRepository, never()).save(any(Car.class));
    }

    @Test
    @DisplayName("Should verify save order in successful reservation")
    void testMakeReservation_SaveOrder() {
        // Given
        String startTime = "2023-10-01T10:00:00";
        String endTime = "2023-10-05T10:00:00";

        when(reservationRepository.save(any(Reservation.class))).thenReturn(testReservation);
        when(customerRepository.save(testCustomer)).thenReturn(testCustomer);
        when(carRepository.save(availableCar)).thenReturn(availableCar);

        // When
        customerService.makeReservation(testCustomer, availableCar, startTime, endTime);

        // Then - Verify save order using InOrder
        InOrder inOrder = inOrder(reservationRepository, customerRepository, carRepository);
        inOrder.verify(reservationRepository).save(any(Reservation.class));
        inOrder.verify(customerRepository).save(testCustomer);
        inOrder.verify(carRepository).save(availableCar);
    }

    @Test
    @DisplayName("Should handle null car gracefully")
    void testMakeReservation_NullCar() {
        // Given
        String startTime = "2023-10-01T10:00:00";
        String endTime = "2023-10-05T10:00:00";

        // When & Then
        assertThrows(NullPointerException.class, () -> {
            customerService.makeReservation(testCustomer, null, startTime, endTime);
        });

        verify(reservationRepository, never()).save(any(Reservation.class));
    }


    @Test
    @DisplayName("Should handle repository save failures")
    void testMakeReservation_RepositorySaveFailure() {
        // Given
        String startTime = "2023-10-01T10:00:00";
        String endTime = "2023-10-05T10:00:00";

        when(reservationRepository.save(any(Reservation.class))).thenThrow(new RuntimeException("Database error"));

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            customerService.makeReservation(testCustomer, availableCar, startTime, endTime);
        });

        // Verify subsequent saves are not called due to exception
        verify(customerRepository, never()).save(any(Customer.class));
        verify(carRepository, never()).save(any(Car.class));
    }

    @Test
    @DisplayName("Should cancel reservation successfully")
    void testCancelReservation_Success() {
        // Given
        String reservationId = "res1";
        when(reservationRepository.findById(reservationId)).thenReturn(Optional.of(testReservation));
        when(reservationRepository.save(testReservation)).thenReturn(testReservation);
        when(carRepository.save(availableCar)).thenReturn(availableCar);
        when(customerRepository.save(testCustomer)).thenReturn(testCustomer);

        // When
        customerService.cancelReservation(reservationId);

        // Then
        verify(reservationRepository, times(1)).findById(reservationId);
        verify(reservationRepository, times(1)).save(testReservation);
        verify(carRepository, times(1)).save(availableCar);
        verify(customerRepository, times(1)).save(testCustomer);

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
            customerService.cancelReservation(reservationId);
        });

        assertEquals("Reservation not found", exception.getMessage());
        verify(reservationRepository, times(1)).findById(reservationId);
        verify(reservationRepository, never()).save(any(Reservation.class));
        verify(carRepository, never()).save(any(Car.class));
        verify(customerRepository, never()).save(any(Customer.class));
    }

    @Test
    @DisplayName("Should handle null reservation ID")
    void testCancelReservation_NullReservationId() {
        // Given
        when(reservationRepository.findById(null)).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            customerService.cancelReservation(null);
        });

        assertEquals("Reservation not found", exception.getMessage());
        verify(reservationRepository, times(1)).findById(null);
    }

    @Test
    @DisplayName("Should handle empty reservation ID")
    void testCancelReservation_EmptyReservationId() {
        // Given
        String emptyReservationId = "";
        when(reservationRepository.findById(emptyReservationId)).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            customerService.cancelReservation(emptyReservationId);
        });

        assertEquals("Reservation not found", exception.getMessage());
        verify(reservationRepository, times(1)).findById(emptyReservationId);
    }

    @Test
    @DisplayName("Should verify save order when cancelling reservation")
    void testCancelReservation_SaveOrder() {
        // Given
        String reservationId = "res1";
        when(reservationRepository.findById(reservationId)).thenReturn(Optional.of(testReservation));
        when(reservationRepository.save(testReservation)).thenReturn(testReservation);
        when(carRepository.save(availableCar)).thenReturn(availableCar);
        when(customerRepository.save(testCustomer)).thenReturn(testCustomer);

        // When
        customerService.cancelReservation(reservationId);

        // Then - Verify save order using InOrder
        InOrder inOrder = inOrder(reservationRepository, carRepository, customerRepository);
        inOrder.verify(reservationRepository).save(testReservation);
        inOrder.verify(carRepository).save(availableCar);
        inOrder.verify(customerRepository).save(testCustomer);
    }

    @Test
    @DisplayName("Should view customer reservations when reservations exist")
    void testViewMyReservations_Success_ReservationsExist() {
        // Given
        String customerId = "customer1";

        Reservation reservation2 = new Reservation();
        reservation2.setReservationId("res2");
        reservation2.setCustomer(testCustomer);
        reservation2.setCar(rentedCar);
        reservation2.setStartTime("2023-11-01T10:00:00");
        reservation2.setEndTime("2023-11-05T10:00:00");
        reservation2.setStatus(Reservation.ReservationStatus.PENDING);
        reservation2.setTotalCost(180.0);

        List<Reservation> reservations = Arrays.asList(testReservation, reservation2);
        when(reservationRepository.findByCustomerId(customerId)).thenReturn(reservations);

        // When
        customerService.viewMyReservations(customerId);

        // Then
        verify(reservationRepository, times(1)).findByCustomerId(customerId);
        // Note: This method only logs, so we verify the repository call
    }

    @Test
    @DisplayName("Should view customer reservations when no reservations exist")
    void testViewMyReservations_Success_NoReservationsExist() {
        // Given
        String customerId = "customer1";
        when(reservationRepository.findByCustomerId(customerId)).thenReturn(Collections.emptyList());

        // When
        customerService.viewMyReservations(customerId);

        // Then
        verify(reservationRepository, times(1)).findByCustomerId(customerId);
    }

    @Test
    @DisplayName("Should handle null customer ID when viewing reservations")
    void testViewMyReservations_NullCustomerId() {
        // Given
        when(reservationRepository.findByCustomerId(null)).thenReturn(Collections.emptyList());

        // When
        customerService.viewMyReservations(null);

        // Then
        verify(reservationRepository, times(1)).findByCustomerId(null);
    }

    @Test
    @DisplayName("Should handle empty customer ID when viewing reservations")
    void testViewMyReservations_EmptyCustomerId() {
        // Given
        String emptyCustomerId = "";
        when(reservationRepository.findByCustomerId(emptyCustomerId)).thenReturn(Collections.emptyList());

        // When
        customerService.viewMyReservations(emptyCustomerId);

        // Then
        verify(reservationRepository, times(1)).findByCustomerId(emptyCustomerId);
    }

    @Test
    @DisplayName("Should handle special characters in customer ID when viewing reservations")
    void testViewMyReservations_SpecialCharactersCustomerId() {
        // Given
        String specialCustomerId = "customer@#$%";
        when(reservationRepository.findByCustomerId(specialCustomerId)).thenReturn(Collections.emptyList());

        // When
        customerService.viewMyReservations(specialCustomerId);

        // Then
        verify(reservationRepository, times(1)).findByCustomerId(specialCustomerId);
    }

    @Test
    @DisplayName("Should handle repository exceptions when viewing reservations")
    void testViewMyReservations_RepositoryException() {
        // Given
        String customerId = "customer1";
        when(reservationRepository.findByCustomerId(customerId)).thenThrow(new RuntimeException("Database error"));

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            customerService.viewMyReservations(customerId);
        });

        verify(reservationRepository, times(1)).findByCustomerId(customerId);
    }

    @Test
    @DisplayName("Should not modify reservation status in makeReservation when car unavailable")
    void testMakeReservation_NoStatusChangeWhenCarUnavailable() {
        // Given
        String startTime = "2023-10-01T10:00:00";
        String endTime = "2023-10-05T10:00:00";
        Car.CarStatus originalStatus = rentedCar.getCarStatus();

        // When
        customerService.makeReservation(testCustomer, rentedCar, startTime, endTime);

        // Then
        assertEquals(originalStatus, rentedCar.getCarStatus());
        verify(reservationRepository, never()).save(any(Reservation.class));
    }

    @Test
    @DisplayName("Should handle multiple concurrent reservation attempts gracefully")
    void testMakeReservation_ConcurrentAttempts() {
        // Given
        String startTime = "2023-10-01T10:00:00";
        String endTime = "2023-10-05T10:00:00";

        when(reservationRepository.save(any(Reservation.class))).thenReturn(testReservation);
        when(customerRepository.save(any(Customer.class))).thenReturn(testCustomer);
        when(carRepository.save(any(Car.class))).thenReturn(availableCar);

        // When - Make multiple reservations
        customerService.makeReservation(testCustomer, availableCar, startTime, endTime);
        customerService.makeReservation(testCustomer, availableCar, startTime, endTime);

        verify(reservationRepository, times(1)).save(any(Reservation.class));
        verify(customerRepository, times(1)).save(testCustomer);
        verify(carRepository, times(1)).save(availableCar);
    }
}