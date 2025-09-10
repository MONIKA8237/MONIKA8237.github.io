package com.example.controller;

import com.example.models.Car;
import com.example.models.Customer;
import com.example.models.Reservation;
import com.example.repository.CarRepository;
import com.example.repository.CustomerRepository;
import com.example.service.CustomerService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class CustomerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CustomerService customerService;

    @MockBean
    private CustomerRepository customerRepository;

    @MockBean
    private CarRepository carRepository;

    private ObjectMapper objectMapper = new ObjectMapper();
    private Reservation testReservation;

    @BeforeEach
    void setUp() {
        // Reset all mocks before each test to ensure clean state
        reset(customerService, customerRepository, carRepository);

        // Setup test reservation
        testReservation = new Reservation();

        Customer customer = new Customer();
        customer.setId("customer1");
        customer.setName("John Doe");
        testReservation.setCustomer(customer);

        Car car = new Car();
        car.setId("car1");
        car.setModel("Toyota Camry");
        testReservation.setCar(car);

        testReservation.setStartTime("2023-10-01T10:00:00");
        testReservation.setEndTime("2023-10-05T10:00:00");
    }

    @Test
    @WithMockUser(username = "customer", roles = {"CUSTOMER"})
    void testViewAllCars() throws Exception {

        // When & Then
        mockMvc.perform(get("/api/customer/cars/available")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        // Verify service interaction
        verify(customerService, times(1)).viewAvailableCars();
    }


    @Test
    @WithMockUser(username = "customer", roles = {"CUSTOMER"})
    void testMakeReservation() throws Exception {
        // Given - Setup mock data
        Customer foundCustomer = new Customer();
        foundCustomer.setId("customer1");
        foundCustomer.setName("John Doe");
        foundCustomer.setEmail("john@example.com");

        Car foundCar = new Car();
        foundCar.setId("car1");
        foundCar.setModel("Toyota Camry");
        foundCar.setBrand("Toyota");

        // Mock repository calls
        when(customerRepository.findById("customer1")).thenReturn(Optional.of(foundCustomer));
        when(carRepository.findById("car1")).thenReturn(Optional.of(foundCar));

        // Mock service method to not throw exception
        doNothing().when(customerService).makeReservation(
                any(Customer.class), any(Car.class), anyString(), anyString()
        );

        // When & Then
        mockMvc.perform(post("/api/customer/reservation")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testReservation)))
                .andExpect(status().isOk())
                .andExpect(content().string("Reservation created successfully"));

        // Verify interactions with ArgumentCaptor for better verification
        verify(customerRepository, times(1)).findById("customer1");
        verify(carRepository, times(1)).findById("car1");

        ArgumentCaptor<Customer> customerCaptor = ArgumentCaptor.forClass(Customer.class);
        ArgumentCaptor<Car> carCaptor = ArgumentCaptor.forClass(Car.class);
        ArgumentCaptor<String> startTimeCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> endTimeCaptor = ArgumentCaptor.forClass(String.class);

        verify(customerService, times(1)).makeReservation(
                customerCaptor.capture(), carCaptor.capture(),
                startTimeCaptor.capture(), endTimeCaptor.capture()
        );

        // Assert captured values
        assertEquals("customer1", customerCaptor.getValue().getId());
        assertEquals("car1", carCaptor.getValue().getId());
        assertEquals("2023-10-01T10:00:00", startTimeCaptor.getValue());
        assertEquals("2023-10-05T10:00:00", endTimeCaptor.getValue());
    }

    @Test
    @WithMockUser(username = "customer", roles = {"CUSTOMER"})
    void testMakeReservation_shouldReturnErrorWhenCustomerNotFound() throws Exception {
        // Given - Mock customer not found, but car exists
        when(customerRepository.findById(anyString())).thenReturn(Optional.empty());

        Car foundCar = new Car();
        foundCar.setId("car1");
        when(carRepository.findById("car1")).thenReturn(Optional.of(foundCar));

        // When & Then - Expect error due to customer not found
        MvcResult result = mockMvc.perform(post("/api/customer/reservation")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testReservation)))
                .andExpect(status().is5xxServerError()) // RuntimeException causes 500
                .andReturn();

        // Verify error message contains expected text (works for both JSON and plain text)
        String responseBody = result.getResponse().getContentAsString();
        assertTrue(responseBody.contains("Customer not found") ||
                responseBody.contains("not found") ||
                responseBody.contains("\"message\""));

        // Verify repository was called but service was not
        verify(customerRepository, times(1)).findById("customer1");
        verify(customerService, never()).makeReservation(any(), any(), any(), any());
    }

    @Test
    @WithMockUser(username = "customer", roles = {"CUSTOMER"})
    void testMakeReservation_shouldReturnErrorWhenCarNotFound() throws Exception {
        // Given - Customer exists, but car not found
        Customer foundCustomer = new Customer();
        foundCustomer.setId("customer1");
        when(customerRepository.findById("customer1")).thenReturn(Optional.of(foundCustomer));

        when(carRepository.findById(anyString())).thenReturn(Optional.empty());

        // When & Then - Expect error due to car not found
        MvcResult result = mockMvc.perform(post("/api/customer/reservation")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testReservation)))
                .andExpect(status().is5xxServerError()) // RuntimeException causes 500
                .andReturn();

        // Verify error message contains expected text
        String responseBody = result.getResponse().getContentAsString();
        assertTrue(responseBody.contains("Car not found") ||
                responseBody.contains("not found") ||
                responseBody.contains("\"message\""));

        // Verify repository interactions
        verify(customerRepository, times(1)).findById("customer1");
        verify(carRepository, times(1)).findById("car1");
        verify(customerService, never()).makeReservation(any(), any(), any(), any());
    }

    @Test
    @WithMockUser(username = "customer", roles = {"CUSTOMER"})
    void testMakeReservation_shouldReturnErrorWhenServiceThrowsException() throws Exception {
        // Given - Both repositories return objects, but service throws exception
        Customer foundCustomer = new Customer();
        foundCustomer.setId("customer1");
        Car foundCar = new Car();
        foundCar.setId("car1");

        when(customerRepository.findById("customer1")).thenReturn(Optional.of(foundCustomer));
        when(carRepository.findById("car1")).thenReturn(Optional.of(foundCar));

        // Service throws exception
        doThrow(new RuntimeException("Car not available for reservation"))
                .when(customerService)
                .makeReservation(any(Customer.class), any(Car.class), anyString(), anyString());

        // When & Then
        MvcResult result = mockMvc.perform(post("/api/customer/reservation")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testReservation)))
                .andExpect(status().is5xxServerError())
                .andReturn();

        // Verify error message
        String responseBody = result.getResponse().getContentAsString();
        assertTrue(responseBody.contains("not available") ||
                responseBody.contains("Car not available"));

        // Verify all components were called
        verify(customerRepository, times(1)).findById("customer1");
        verify(carRepository, times(1)).findById("car1");
        verify(customerService, times(1)).makeReservation(any(), any(), any(), any());
    }

    @Test
    @WithMockUser(username = "customer", roles = {"CUSTOMER"})
    void testCancelReservation() throws Exception {
        // Given
        String reservationId = "res123";
        doNothing().when(customerService).cancelReservation(reservationId);

        // When & Then
        mockMvc.perform(delete("/api/customer/reservation/{id}", reservationId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string("Reservation cancelled"));

        // Verify service method was called with correct parameter
        verify(customerService, times(1)).cancelReservation(reservationId);
    }

    @Test
    @WithMockUser(username = "customer", roles = {"CUSTOMER"})
    void testCancelReservation_ServiceException() throws Exception {
        // Given - Service throws exception
        String reservationId = "res123";
        doThrow(new RuntimeException("Reservation not found or cannot be cancelled"))
                .when(customerService).cancelReservation(reservationId);

        // When & Then
        MvcResult result = mockMvc.perform(delete("/api/customer/reservation/{id}", reservationId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is5xxServerError())
                .andReturn();

        // Verify error message is present
        String responseBody = result.getResponse().getContentAsString();
        assertTrue(responseBody.contains("not found") ||
                responseBody.contains("cannot be cancelled"));

        // Verify service was called
        verify(customerService, times(1)).cancelReservation(reservationId);
    }

    @Test
    @WithMockUser(username = "customer", roles = {"CUSTOMER"})
    void testViewMyReservation() throws Exception {
        // Given
        String customerId = "customer1";
        doNothing().when(customerService).viewMyReservations(customerId);

        // When & Then
        mockMvc.perform(get("/api/customer/reservations/{customerId}", customerId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string("Reservations displayed in console"));

        // Verify service method was called with correct parameter
        verify(customerService, times(1)).viewMyReservations(customerId);
    }

    @Test
    @WithMockUser(username = "customer", roles = {"CUSTOMER"})
    void testViewMyReservation_ServiceException() throws Exception {
        // Given - Service throws exception
        String customerId = "customer1";
        doThrow(new RuntimeException("Customer has no reservations"))
                .when(customerService).viewMyReservations(customerId);

        // When & Then
        MvcResult result = mockMvc.perform(get("/api/customer/reservations/{customerId}", customerId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is5xxServerError())
                .andReturn();

        // Verify error message
        String responseBody = result.getResponse().getContentAsString();
        assertTrue(responseBody.contains("no reservations") ||
                responseBody.contains("Customer"));

        // Verify service was called
        verify(customerService, times(1)).viewMyReservations(customerId);
    }

    @Test
    @WithMockUser(username = "customer", roles = {"CUSTOMER"})
    void testMakeReservation_WithMissingFields() throws Exception {
        // Given - Reservation with missing customer
        Reservation invalidReservation = new Reservation();
        Car car = new Car();
        car.setId("car1");
        invalidReservation.setCar(car);
        invalidReservation.setStartTime("2023-10-01T10:00:00");
        invalidReservation.setEndTime("2023-10-05T10:00:00");
        // Missing customer

        // When & Then - Should handle null customer gracefully
        mockMvc.perform(post("/api/customer/reservation")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidReservation)))
                .andExpect(status().is5xxServerError()); // NullPointerException or similar

        // Verify repositories were not called properly due to null customer
        verify(customerService, never()).makeReservation(any(), any(), any(), any());
    }
}
