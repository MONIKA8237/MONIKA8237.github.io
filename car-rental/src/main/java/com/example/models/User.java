package com.example.models;

import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "user_type")
@Data
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public abstract class User {
    String name;
    String email;
    String phoneNumber;
    @Id
    private String id;

    public User(String id, String name, String email, String phoneNumber) {
        this.id=id;
        this.name = name;
        this.email = email;
        this.phoneNumber = phoneNumber;
    }

    public abstract String getUserType();
    public abstract boolean canManageCars();

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }
}
