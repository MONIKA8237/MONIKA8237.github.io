package com.example.service.impl;

import com.example.models.Car;
import com.example.models.Customer;
import com.example.models.Reservation;
import com.example.repository.CarRepository;
import com.example.repository.CustomerRepository;
import com.example.repository.ReservationRepository;
import com.example.service.AdminService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminServiceImpl implements AdminService {
    private static final Logger log = LoggerFactory.getLogger(AdminServiceImpl.class);
    private final ReservationRepository reservationRepository;
    private final CarRepository carRepository;
    private final CustomerRepository customerRepository;

    @Override
    public void addCar(Car car) {
        carRepository.save(car);
        log.info("Car with ID: {} added successfully.", car.getId());
    }

    @Override
    public void removeCar(Car car) {
        List<Reservation> reservations = reservationRepository.findByCarId(car.getId());
        boolean hasActive = reservations.stream()
                .anyMatch(r -> r.getStatus() != Reservation.ReservationStatus.CANCELLED);


        if (hasActive) {
            throw new IllegalStateException("Cannot remove car with active reservations");
        }

        carRepository.delete(car);
        log.info("Car {} removed successfully: " , car.getModel());
    }

    @Override
    public void updateCarDetails(Car car) {
        carRepository.save(car);
        log.info("Car details updated successfully for {}: " , car.getModel());
    }

    @Override
    public List<Car> viewAllCars() {
        List<Car> allCars = carRepository.findAll();

        if (allCars.isEmpty()) {
            log.info("No cars in the system.");
        } else {
            allCars.forEach(car ->
                    log.info(String.format("ID: %s | Model: %s | Type: %s | Status: %s | Rate: $%s/hour",
                            car.getId(), car.getModel(), car.getCarType(),
                            car.getCarStatus(), car.getRatePerHour()))
            );
        }
        return allCars;
    }

    @Override
    public List<Reservation> viewAllReservations() {
        List<Reservation> allReservations = reservationRepository.findAll();

        if (allReservations.isEmpty()) {
            log.info("No reservations in the system.");
        } else {
            allReservations.forEach(res ->
                    log.info(String.format("ID: %s | Customer: %s | Car: %s | Status: %s | Cost: $%s",
                            res.getReservationId(), res.getCustomer().getName(),
                            res.getCar().getModel(), res.getStatus(), res.getTotalCost()))
            );
        }
        return allReservations;
    }

    @Override
    public void makeReservationForCustomer(Customer customer, Car car, String startTime, String endTime) {
        if (car.getCarStatus() != Car.CarStatus.AVAILABLE) {
            System.out.println("Error: Car is not available for reservation.");
            return;
        }

        Reservation reservation = new Reservation();
        reservation.setCar(car);
        reservation.setCustomer(customer);
        reservation.setStartTime(startTime);
        reservation.setEndTime(endTime);
        reservation.setStatus(Reservation.ReservationStatus.PENDING);

        reservation.calculateTotalCost();
        reservation.confirm();

        reservationRepository.save(reservation);
        customer.addReservation(reservation);
        customerRepository.save(customer);
        carRepository.save(car);

        System.out.println("Admin reservation created! ID: " + reservation.getReservationId());
    }

    @Override
    public void cancelReservation(String reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new RuntimeException("Reservation not found"));

        reservation.cancel();
        reservationRepository.save(reservation);
        carRepository.save(reservation.getCar());

        System.out.println("Admin cancelled reservation: " + reservationId);
    }
}
