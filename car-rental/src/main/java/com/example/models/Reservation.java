package com.example.models;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Entity
@Table(name = "reservations")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Reservation implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String reservationId;

    @ManyToOne
    @JoinColumn(name = "car_id")
    private Car car;

    @ManyToOne
    @JoinColumn(name = "customer_id")
    private Customer customer;

    private String startTime;
    private String endTime;
    @Enumerated(EnumType.STRING)
    private ReservationStatus status;
    private double totalCost;

    public enum ReservationStatus {
        PENDING,
        CONFIRMED,
        CANCELLED
    }

    public void confirm() {
        if (status != ReservationStatus.PENDING) {
            throw new IllegalStateException("Can only confirm pending reservations");
        }
        this.status = ReservationStatus.CONFIRMED;
        this.car.reserve();
    }

    public void cancel() {
        if (status == ReservationStatus.CANCELLED) {
            throw new IllegalStateException("Reservation already cancelled");
        }
        this.status = ReservationStatus.CANCELLED;
        this.car.makeAvailable();

        if (null != customer && null != customer.getReservationList()) {
            if (!customer.getReservationList().isEmpty()) {
                customer.getReservationList().remove(this);
            }
        }

    }

    public double calculateTotalCost() {
        long hours = ChronoUnit.HOURS.between(LocalDateTime.parse(startTime), LocalDateTime.parse(endTime));
        if (hours == 0) hours = 1;
        double cost = hours * car.getRatePerHour();
        this.totalCost =cost;
        return this.totalCost;
    }
}
