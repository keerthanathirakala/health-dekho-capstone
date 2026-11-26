package com.healthdekho.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public final class AuthDtos {
    private AuthDtos() {}
    public record LoginRequest(@NotBlank @Email String email, @NotBlank String password) {}
    public record LoginResponse(String token, String tokenType, long expiresInSeconds,
                                CustomerInfo customer) {}
    public record CustomerInfo(Long customerId, String email, String fullName,
                               java.time.LocalDate dateOfBirth, String phone) {}
}
