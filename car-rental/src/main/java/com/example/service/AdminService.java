package com.example.service;

import com.example.models.Car;
import com.example.models.Customer;
import com.example.models.Reservation;

import java.time.LocalDateTime;
import java.util.List;

public interface AdminService {

    void addCar(Car car);

    void removeCar(Car car);

    void updateCarDetails(Car car);

    List<Car> viewAllCars();

    List<Reservation> viewAllReservations();

    void makeReservationForCustomer(Customer customer, Car car, String startTime, String endTime);

    void cancelReservation(String reservationId);
}
