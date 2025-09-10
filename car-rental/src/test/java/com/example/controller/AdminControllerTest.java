package com.example.controller;

import com.example.models.Car;
import com.example.models.Customer;
import com.example.models.Reservation;
import com.example.repository.CustomerRepository;
import com.example.service.AdminService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AdminService adminService;

    @MockBean
    private CustomerRepository customerRepository;

    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        // Reset all mocks before each test to ensure clean state
        reset(adminService, customerRepository);
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testAddCar() throws Exception {
        // Given
        Car car = new Car();
        car.setId("CAR123");
        car.setBrand("Toyota");
        car.setModel("Camry");

        doNothing().when(adminService).addCar(any(Car.class));

        // When & Then
        mockMvc.perform(post("/api/admin/car")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(car)))
                .andExpect(status().isOk())
                .andExpect(content().string("Car added successfully"));

        // Verify service interaction
        ArgumentCaptor<Car> carCaptor = ArgumentCaptor.forClass(Car.class);
        verify(adminService, times(1)).addCar(carCaptor.capture());
        assertEquals("CAR123", carCaptor.getValue().getId());
        assertEquals("Toyota", carCaptor.getValue().getBrand());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testGetAllCars() throws Exception {
        // Given
        Car car1 = new Car();
        car1.setId("CAR1");
        car1.setBrand("Toyota");

        Car car2 = new Car();
        car2.setId("CAR2");
        car2.setBrand("Honda");

        when(adminService.viewAllCars()).thenReturn(List.of(car1, car2));

        // When & Then
        mockMvc.perform(get("/api/admin/cars")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));

        // Verify service interaction
        verify(adminService, times(1)).viewAllCars();
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testDeleteCar() throws Exception {
        // Given
        Car car = new Car();
        car.setId("CAR123");
        car.setBrand("Toyota");

        doNothing().when(adminService).removeCar(any(Car.class));

        // When & Then
        mockMvc.perform(delete("/api/admin/car")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(car)))
                .andExpect(status().isOk())
                .andExpect(content().string("Car removed successfully"));

        // Verify service interaction
        ArgumentCaptor<Car> carCaptor = ArgumentCaptor.forClass(Car.class);
        verify(adminService, times(1)).removeCar(carCaptor.capture());
        assertEquals("CAR123", carCaptor.getValue().getId());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testUpdateCar() throws Exception {
        // Given
        Car car = new Car();
        car.setId("CAR123");
        car.setBrand("Honda");
        car.setModel("Civic");

        doNothing().when(adminService).updateCarDetails(any(Car.class));

        // When & Then
        mockMvc.perform(put("/api/admin/car")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(car)))
                .andExpect(status().isOk())
                .andExpect(content().string("Car updated successfully"));

        // Verify service interaction
        ArgumentCaptor<Car> carCaptor = ArgumentCaptor.forClass(Car.class);
        verify(adminService, times(1)).updateCarDetails(carCaptor.capture());
        assertEquals("CAR123", carCaptor.getValue().getId());
        assertEquals("Honda", carCaptor.getValue().getBrand());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testGetAllReservations_Success() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/admin/reservations")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string("Reservations displayed in console"));

        // Verify service interaction
        verify(adminService, times(1)).viewAllReservations();
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testMakeReservationForCustomer_Success() throws Exception {
        // Given - Setup test data
        String customerId = "customer1";
        String carId = "cc1a35f3-c00f-4262-b85a-9fb72f77041a";
        String startTime = "2024-01-20T10:00:00";
        String endTime = "2024-01-20T18:00:00";

        Customer customer = new Customer();
        customer.setId(customerId);
        customer.setName("John Doe");
        customer.setEmail("john@example.com");

        // Mock repository response
        when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));

        // Mock service method
        doNothing().when(adminService).makeReservationForCustomer(
                any(Customer.class), any(Car.class), anyString(), anyString());

        // Create request body
        Reservation request = new Reservation();

        Customer requestCustomer = new Customer();
        requestCustomer.setId(customerId);
        request.setCustomer(requestCustomer);

        Car requestCar = new Car();
        requestCar.setId(carId);
        request.setCar(requestCar);

        request.setStartTime(startTime);
        request.setEndTime(endTime);

        // When & Then
        mockMvc.perform(post("/api/admin/reservation/{customerId}", customerId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string("Reservation created by admin"));

        // Verify repository interaction
        verify(customerRepository, times(1)).findById(customerId);

        // Verify service was called with correct parameters
        ArgumentCaptor<Customer> customerCaptor = ArgumentCaptor.forClass(Customer.class);
        ArgumentCaptor<Car> carCaptor = ArgumentCaptor.forClass(Car.class);
        ArgumentCaptor<String> startCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> endCaptor = ArgumentCaptor.forClass(String.class);

        verify(adminService, times(1)).makeReservationForCustomer(
                customerCaptor.capture(),
                carCaptor.capture(),
                startCaptor.capture(),
                endCaptor.capture()
        );

        // Assert captured values
        assertEquals(customerId, customerCaptor.getValue().getId());
        assertEquals(carId, carCaptor.getValue().getId());
        assertEquals(startTime, startCaptor.getValue());
        assertEquals(endTime, endCaptor.getValue());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testMakeReservationForCustomer_CustomerNotFound() throws Exception {
        String customerId = "nonexistent";
        String carId = "car1";

        when(customerRepository.findById(customerId)).thenReturn(Optional.empty());

        Reservation request = new Reservation();

        Customer requestCustomer = new Customer();
        requestCustomer.setId(customerId);
        request.setCustomer(requestCustomer);

        Car requestCar = new Car();
        requestCar.setId(carId);
        request.setCar(requestCar);


        // When & Then - Should return error for non-existent customer
        mockMvc.perform(post("/api/admin/reservation/{customerId}", customerId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(result -> {
                    String responseBody = result.getResponse().getContentAsString();
                    // Check that error message is present in some form
                    assertTrue(responseBody.contains("not found") ||
                            responseBody.contains("NONEXISTENT"));
                });

        // Verify repository was called but service was not
        verify(customerRepository, times(1)).findById(customerId);
        verify(adminService, never()).makeReservationForCustomer(any(), any(), any(), any());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testCancelReservation_Success() throws Exception {
        // Given
        String reservationId = "RES123";
        doNothing().when(adminService).cancelReservation(reservationId);

        // When & Then
        mockMvc.perform(delete("/api/admin/reservation/{id}", reservationId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string("Reservation cancelled by admin"));

        // Verify service interaction
        verify(adminService, times(1)).cancelReservation(reservationId);
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testCancelReservation_ServiceException() throws Exception {
        // Given - Service throws exception
        String reservationId = "RES123";
        doThrow(new RuntimeException("Reservation not found"))
                .when(adminService).cancelReservation(reservationId);

        // When & Then - Should handle service exception
        mockMvc.perform(delete("/api/admin/reservation/{id}", reservationId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(result -> {
                    String responseBody = result.getResponse().getContentAsString();
                    // This works for both JSON and plain text responses
                    assertTrue(responseBody.contains("Reservation not found") ||
                            responseBody.contains("\"message\":\"Reservation not found\""));
                });

        // Verify service was called
        verify(adminService, times(1)).cancelReservation(reservationId);
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testCancelReservation_ReservationNotFound() throws Exception {
        String reservationId = "NONEXISTENT";
        doThrow(new RuntimeException("Reservation with ID NONEXISTENT not found"))
                .when(adminService).cancelReservation(reservationId);

        mockMvc.perform(delete("/api/admin/reservation/{id}", reservationId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(result -> {
                    String responseBody = result.getResponse().getContentAsString();
                    // Check that error message is present in some form
                    assertTrue(responseBody.contains("not found") ||
                            responseBody.contains("NONEXISTENT"));
                });

        verify(adminService, times(1)).cancelReservation(reservationId);
    }
}


