package com.example.data;

import com.example.models.Admin;
import com.example.models.Car;
import com.example.models.Customer;
import com.example.repository.AdminRepository;
import com.example.repository.CarRepository;
import com.example.repository.CustomerRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer {

    private final CarRepository carRepository;
    private final CustomerRepository customerRepository;
    private final AdminRepository adminRepository;

    public DataInitializer(CarRepository carRepository,
                           CustomerRepository customerRepository,
                           AdminRepository adminRepository) {
        this.carRepository = carRepository;
        this.customerRepository = customerRepository;
        this.adminRepository = adminRepository;
    }

    @Bean
    CommandLineRunner initDatabase() {
        return args -> {
            Car car1 = new Car("car1","Camry", "Toyota", Car.CarStatus.AVAILABLE, 25.00, Car.CarType.SEDAN);
            Car car2 = new Car("car2","CR-V", "Honda", Car.CarStatus.AVAILABLE, 35.00, Car.CarType.SUV);
            Car car3 = new Car("car3","F-150", "Ford", Car.CarStatus.AVAILABLE, 45.00, Car.CarType.TRUCK);

            carRepository.save(car1);
            carRepository.save(car2);
            carRepository.save(car3);

            Customer customer = new Customer("customer1","John Doe", "john@example.com", "123-456-7890", "DL123456");
            customerRepository.save(customer);

            Admin admin = new Admin("Admin1","Admin User", "admin@example.com", "098-765-4321", "ADMIN123");
            adminRepository.save(admin);

            System.out.println("Sample data initialized!");
        };
    }
}
