package com.example.service;

import com.example.models.Car;
import com.example.models.Customer;

import java.time.LocalDateTime;
import java.util.List;

public interface CustomerService {

    List<Car> viewAvailableCars();

    void makeReservation(Customer customer, Car car, String  startTime, String endTime);

    void cancelReservation(String reservationId);

    void viewMyReservations(String customerId);
}
