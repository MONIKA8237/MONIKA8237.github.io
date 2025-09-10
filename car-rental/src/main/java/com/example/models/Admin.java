package com.example.models;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@DiscriminatorValue("ADMIN")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class Admin extends User {

    private String adminCode;

    public Admin(String adminId,String name, String email, String phoneNumber, String adminCode) {
        super(adminId,name, email, phoneNumber);
        this.adminCode = adminCode;
    }

    @Override
    public String getUserType() {
        return "ADMIN";
    }

    @Override
    public boolean canManageCars() {
        return true;
    }
}
