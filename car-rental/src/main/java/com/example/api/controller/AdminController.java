package com.example.api.controller;

import com.example.models.Car;
import com.example.models.Customer;
import com.example.models.Reservation;
import com.example.repository.CustomerRepository;
import com.example.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;
    private final CustomerRepository customerRepository;

    @PostMapping("/car")
    public ResponseEntity<String> addCar(@RequestBody Car car) {
        adminService.addCar(car);
        return ResponseEntity.ok("Car added successfully");
    }

    @DeleteMapping("/car")
    public ResponseEntity<String> removeCar(@RequestBody Car car) {
        adminService.removeCar(car);
        return ResponseEntity.ok("Car removed successfully");
    }

    @PutMapping("/car")
    public ResponseEntity<String> updateCar(@RequestBody Car car) {
        adminService.updateCarDetails(car);
        return ResponseEntity.ok("Car updated successfully");
    }

    @GetMapping("/cars")
    public ResponseEntity<List<Car>> getAllCars() {
        return ResponseEntity.ok(adminService.viewAllCars());
    }

    @GetMapping("/reservations")
    public ResponseEntity<String> getAllReservations() {
        adminService.viewAllReservations();
        return ResponseEntity.ok("Reservations displayed in console");
    }

    @PostMapping("/reservation/{customerId}")
    public ResponseEntity<String> makeReservationForCustomer(@PathVariable String customerId, @RequestBody Reservation request) {
            Customer customer = customerRepository.findById(customerId).orElseThrow(
                    () -> new RuntimeException("Customer not found"));

            Car car = new Car();
            car.setId(request.getCar().getId());

            adminService.makeReservationForCustomer(customer, car, request.getStartTime(),
                    request.getEndTime());

            return ResponseEntity.ok("Reservation created by admin");
        }

    @DeleteMapping("/reservation/{id}")
    public ResponseEntity<String> cancelReservation(@PathVariable String id) {
        adminService.cancelReservation(id);
        return ResponseEntity.ok("Reservation cancelled by admin");
    }
}