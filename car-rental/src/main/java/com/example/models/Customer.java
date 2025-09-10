package com.example.models;

import jakarta.persistence.CascadeType;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@DiscriminatorValue("CUSTOMER")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class Customer extends User{
    private String licenseNumber;

    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL)
    private List<Reservation> reservationList;


    public Customer(String id, String name, String email, String phoneNumber,String licenseNumber) {
        super(id, name, email, phoneNumber);
        this.licenseNumber = licenseNumber;
    }

    @Override
    public String getUserType() {
        return "CUSTOMER";
    }

    @Override
    public boolean canManageCars() {
        return false;
    }
    public void addReservation(Reservation reservation) {
        if(this.reservationList == null) {
            this.reservationList = new java.util.ArrayList<>();
        }
        this.reservationList.add(reservation);
    }

    public boolean hasActiveReservations() {
        return reservationList.stream()
                .anyMatch(r -> r.getStatus() == Reservation.ReservationStatus.CONFIRMED);
    }

}

