package com.example.repository;

import com.example.models.Car;
import com.example.models.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, String> {
    List<Reservation> findByCustomerId(String customerId);
    List<Reservation> findByCarId(String carId);
    List<Reservation> findByStatus(Reservation.ReservationStatus status);
}
