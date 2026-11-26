package com.healthdekho.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public final class PremiumDtos {
    private PremiumDtos() {}
    public record PayRequest(@NotNull @Positive Long installmentId,
                             @NotNull @DecimalMin("0.01") @Digits(integer = 12, fraction = 2) BigDecimal amount,
                             @NotBlank @Size(max = 30) String paymentMethod) {}
    public record InstallmentResponse(Long installmentId, String policyNumber, int installmentNumber,
                                      LocalDate dueDate, LocalDate gracePeriodEndDate,
                                      BigDecimal scheduledAmount, BigDecimal paidAmount,
                                      String paymentStatus) {}
    public record PaymentResponse(Long paymentId, String policyNumber, String policyName,
                                  Integer installmentNumber, String transactionReference,
                                  Instant paymentDate, BigDecimal amountPaid, BigDecimal penaltyAmount,
                                  String paymentMethod) {}
}
