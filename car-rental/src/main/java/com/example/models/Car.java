package com.example.models;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "cars")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Car {

    public enum CarType {
        SEDAN,
        SUV,
        TRUCK
    }

    public enum CarStatus {
        AVAILABLE,
        RESERVED,
        RENTED,
        MAINTENANCE
    }
    @Id
    private String id;
    private String model;
    private String brand;
    private double ratePerHour;
    @Enumerated(EnumType.STRING)
    private CarStatus carStatus;
    @Enumerated(EnumType.STRING)
    private CarType carType;

    public Car (String id,String model, String brand, CarStatus carStatus, double ratePerHour,CarType carType) {
        this.id=id;
        this.model = model;
        this.brand = brand;
        this.carStatus = carStatus;
        this.ratePerHour = ratePerHour;
        this.carType = carType;
    }

    public boolean isAvailable() {
        return carStatus == CarStatus.AVAILABLE;
    }

    public void reserve() {
        if (!isAvailable()) {
            throw new IllegalStateException("Car is not available");
        }
        this.carStatus = CarStatus.RESERVED;
    }

    public void makeAvailable() {
        this.carStatus = CarStatus.AVAILABLE;
    }


}
