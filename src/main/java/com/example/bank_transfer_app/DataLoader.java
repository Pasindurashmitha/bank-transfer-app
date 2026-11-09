
package com.example.bank_transfer_app;

import com.example.bank_transfer_app.entity.Customer;
import com.example.bank_transfer_app.repository.CustomerRepository;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class DataLoader implements CommandLineRunner {

    private final CustomerRepository customerRepository;

    // Constructor injection
    public DataLoader(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    @Override
    public void run(String... args) {

        // Customer 1
        if (customerRepository.findByAccountNumber("1001").isEmpty()) {

            Customer customer1 = new Customer();
            customer1.setAccountNumber("1001");
            customer1.setName("Pasindu");
            customer1.setEmail("pasindu@gmail.com");
            customer1.setBalance(new BigDecimal("50000.00"));

            customerRepository.save(customer1);
        }

        // Customer 2
        if (customerRepository.findByAccountNumber("1002").isEmpty()) {

            Customer customer2 = new Customer();
            customer2.setAccountNumber("1002");
            customer2.setName("Kamal");
            customer2.setEmail("kamal@gmail.com");
            customer2.setBalance(new BigDecimal("25000.00"));

            customerRepository.save(customer2);
        }

        // Customer 3
        if (customerRepository.findByAccountNumber("1003").isEmpty()) {

            Customer customer3 = new Customer();
            customer3.setAccountNumber("1003");
            customer3.setName("Nimal");
            customer3.setEmail("nimal@gmail.com");
            customer3.setBalance(new BigDecimal("10000.00"));

            customerRepository.save(customer3);
        }

        System.out.println("Initial customer data loaded successfully!");
    }
}
