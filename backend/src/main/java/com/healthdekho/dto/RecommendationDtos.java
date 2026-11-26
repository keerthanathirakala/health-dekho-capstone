package com.healthdekho.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public final class RecommendationDtos {
    private RecommendationDtos() {}
    public record GenerateRequest(@NotNull @PastOrPresent LocalDate dateOfBirth,
                                  @NotBlank @jakarta.validation.constraints.Size(max = 1000) String insuranceGoals,
                                  @NotNull @Positive Integer preferredTerm,
                                  String preferredPolicyType) {}
    public record RecommendedProduct(Long productId, String productCode, String policyName,
                                     String policyType, int termMonths, String premiumFrequency,
                                     BigDecimal premiumAmount, BigDecimal sumInsured, int rank,
                                     String suitability, String reason) {}
    public record RecommendationResponse(Long recommendationRequestId, LocalDate requestedAt,
                                         List<RecommendedProduct> recommendations) {}
}
