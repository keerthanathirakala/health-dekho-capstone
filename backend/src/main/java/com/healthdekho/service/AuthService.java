package com.healthdekho.service;

import com.healthdekho.domain.CustomerStatus;
import com.healthdekho.dto.AuthDtos;
import com.healthdekho.mapper.ApiMapper;
import com.healthdekho.repository.CustomerRepository;
import com.healthdekho.security.JwtService;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    private final CustomerRepository customers;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final ApiMapper mapper;

    public AuthService(CustomerRepository customers, PasswordEncoder passwordEncoder, JwtService jwtService, ApiMapper mapper) {
        this.customers = customers;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.mapper = mapper;
    }

    public AuthDtos.LoginResponse login(AuthDtos.LoginRequest request) {
                System.out.println("REAL HASH: " + passwordEncoder.encode("Pass@123"));
        var customer = customers.findByEmailIgnoreCase(request.email())
                .filter(c -> passwordEncoder.matches(request.password(), c.getPasswordHash()))
                .filter(c -> c.getStatus() == CustomerStatus.ACTIVE)
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));
        return new AuthDtos.LoginResponse(jwtService.issue(customer.getEmail()), "Bearer",
                jwtService.expirationSeconds(), mapper.toCustomerInfo(customer));
    }
}
