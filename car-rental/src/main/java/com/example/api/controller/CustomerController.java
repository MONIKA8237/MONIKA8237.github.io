package com.example.api.controller;

import com.example.models.Car;
import com.example.models.Customer;
import com.example.models.Reservation;
import com.example.repository.CarRepository;
import com.example.repository.CustomerRepository;
import com.example.service.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/customer")
@RequiredArgsConstructor
public class CustomerController {
    private final CustomerService customerService;
    private final CustomerRepository customerRepository;
    private final CarRepository carRepository;

    @GetMapping("/cars/available")
    public ResponseEntity<List<Car>> viewAvailableCars() {
        return ResponseEntity.ok(customerService.viewAvailableCars());
    }

    @PostMapping("/reservation")
    public ResponseEntity<String> makeReservation(@RequestBody Reservation request) {
            Customer customer = customerRepository.findById(request.getCustomer().getId()).orElseThrow(
                    () -> new RuntimeException("Customer not found"));
            Car car = carRepository.findById(request.getCar().getId()).orElseThrow(
                    () -> new RuntimeException("Car not found"));

            customerService.makeReservation(customer, car, request.getStartTime(), request.getEndTime());

            return ResponseEntity.ok("Reservation created successfully");
    }

    @DeleteMapping("/reservation/{id}")
    public ResponseEntity<String> cancelReservation(@PathVariable String id) {
        customerService.cancelReservation(id);
        return ResponseEntity.ok("Reservation cancelled");
    }

    @GetMapping("/reservations/{customerId}")
    public ResponseEntity<String> viewMyReservations(@PathVariable String customerId) {
        customerService.viewMyReservations(customerId);
        return ResponseEntity.ok("Reservations displayed in console");
    }

}
