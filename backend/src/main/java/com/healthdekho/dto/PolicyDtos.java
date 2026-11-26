package com.healthdekho.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDate;

public final class PolicyDtos {
    private PolicyDtos() {}
    public record RegisterRequest(@NotNull @Positive Long productId) {}
    public record PolicyResponse(Long policyId, String policyNumber, String productCode,
                                 String policyName, String policyType, int termMonths,
                                 String premiumFrequency, BigDecimal premiumAmount,
                                 BigDecimal sumInsured, BigDecimal totalPremiumPayable,
                                 String status, LocalDate startDate, LocalDate maturityDate) {}
    public record RegistrationResponse(String message, PolicyResponse policy) {}
}
