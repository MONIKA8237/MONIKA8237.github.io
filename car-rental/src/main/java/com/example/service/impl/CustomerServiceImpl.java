package com.example.service.impl;

import com.example.models.Car;
import com.example.models.Customer;
import com.example.models.Reservation;
import com.example.repository.CarRepository;
import com.example.repository.CustomerRepository;
import com.example.repository.ReservationRepository;
import com.example.service.CustomerService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class CustomerServiceImpl implements CustomerService {
    private static final Logger log = LoggerFactory.getLogger(CustomerServiceImpl.class);
    private final ReservationRepository reservationRepository;
    private final CarRepository carRepository;
    private final CustomerRepository customerRepository;

    @Override
    public List<Car> viewAvailableCars() {
        List<Car> availableCars=
                carRepository.findAll().stream().filter(car -> car.getCarStatus() == Car.CarStatus.AVAILABLE).toList();
        if (availableCars.isEmpty()) {
            log.info("No cars currently available.");
        } else {
            availableCars.forEach(car ->
                    log.info("Available cars are:"+ String.format("ID: %s | Model: %s | Type: %s | Rate: $%s/hour",
                            car.getId(), car.getModel(), car.getCarType(), car.getRatePerHour()))
            );
        }
        return availableCars;
    }

    @Override
    public void makeReservation(Customer customer, Car car, String startTime, String endTime) {
        if (car.getCarStatus() != Car.CarStatus.AVAILABLE) {
            log.info("Car with ID: {} is not available for reservation.", car.getId());
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


        log.info("Reservation successful! Reservation ID: {}", reservation.getReservationId());
    }

    @Override
    public void cancelReservation(String reservationId) {
        var reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new RuntimeException("Reservation not found"));

        reservation.cancel();
        reservationRepository.save(reservation);

        carRepository.save(reservation.getCar());
        customerRepository.save(reservation.getCustomer());
        log.info("Reservation with ID: {} has been cancelled.", reservationId);
    }

    @Override
    public void viewMyReservations(String customerId) {
        List<Reservation> reservations = reservationRepository.findByCustomerId(customerId);
        reservations.forEach(reservation ->
                    log.info(String.format("ID: %s | Car: %s | Start: %s | End: %s | Status: %s | Cost: $%s",
                            reservation.getReservationId(), reservation.getCar().getModel(), reservation.getStartTime(),
                            reservation.getEndTime(), reservation.getStatus(), reservation.getTotalCost()))
            );
    }
}
