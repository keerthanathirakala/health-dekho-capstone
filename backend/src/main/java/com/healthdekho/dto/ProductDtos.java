package com.healthdekho.dto;

import java.math.BigDecimal;

public final class ProductDtos {
    private ProductDtos() {}
    public record ProductResponse(Long productId, String productCode, String policyName,
                                  String policyType, int termMonths, String premiumFrequency,
                                  BigDecimal premiumAmount, BigDecimal sumInsured,
                                  int minEntryAge, int maxEntryAge) {}
}
