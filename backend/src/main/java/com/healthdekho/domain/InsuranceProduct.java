package com.healthdekho.domain;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "insurance_product")
@Getter @Setter @NoArgsConstructor(access = AccessLevel.PROTECTED)
public class InsuranceProduct {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_id")
    private Long id;
    @Column(name = "product_code", nullable = false, unique = true, length = 40)
    private String productCode;
    @Column(name = "policy_name", nullable = false, length = 160)
    private String policyName;
    @Enumerated(EnumType.STRING) @Column(name = "policy_type", nullable = false, length = 30)
    private PolicyType policyType;
    @Column(name = "term_months", nullable = false)
    private int termMonths;
    @Enumerated(EnumType.STRING) @Column(name = "premium_frequency", nullable = false, length = 20)
    private PremiumFrequency premiumFrequency;
    @Column(name = "premium_amount", nullable = false, precision = 14, scale = 2)
    private BigDecimal premiumAmount;
    @Column(name = "sum_insured", nullable = false, precision = 16, scale = 2)
    private BigDecimal sumInsured;
    @Column(name = "min_entry_age", nullable = false)
    private int minEntryAge;
    @Column(name = "max_entry_age", nullable = false)
    private int maxEntryAge;
    @Column(nullable = false)
    private boolean active;
    @OneToMany(mappedBy = "product")
    private List<CustomerPolicy> customerPolicies = new ArrayList<>();
    @OneToMany(mappedBy = "product")
    private List<AiRecommendationItem> recommendationItems = new ArrayList<>();

    public InsuranceProduct(String productCode, String policyName, PolicyType policyType, int termMonths,
                            PremiumFrequency premiumFrequency, BigDecimal premiumAmount, BigDecimal sumInsured,
                            int minEntryAge, int maxEntryAge) {
        this.productCode = productCode;
        this.policyName = policyName;
        this.policyType = policyType;
        this.termMonths = termMonths;
        this.premiumFrequency = premiumFrequency;
        this.premiumAmount = premiumAmount;
        this.sumInsured = sumInsured;
        this.minEntryAge = minEntryAge;
        this.maxEntryAge = maxEntryAge;
        this.active = true;
    }
}
