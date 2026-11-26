package com.healthdekho.service;

import com.healthdekho.domain.Customer;
import com.healthdekho.exception.ApiException;
import com.healthdekho.repository.CustomerRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class CustomerAccessService {
    private final CustomerRepository customers;
    public CustomerAccessService(CustomerRepository customers) { this.customers = customers; }

    public Customer requireCustomer(String email) {
        return customers.findByEmailIgnoreCase(email)
                .filter(customer -> customer.getStatus() == com.healthdekho.domain.CustomerStatus.ACTIVE)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Customer account was not found or is inactive"));
    }
}
