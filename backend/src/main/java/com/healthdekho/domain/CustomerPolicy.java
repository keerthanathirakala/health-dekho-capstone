package com.healthdekho.domain;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "customer_policy")
@Getter @Setter @NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CustomerPolicy {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "policy_id")
    private Long id;
    @Column(name = "policy_number", nullable = false, unique = true, length = 40)
    private String policyNumber;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private InsuranceProduct product;
    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;
    @Column(name = "maturity_date", nullable = false)
    private LocalDate maturityDate;
    @Enumerated(EnumType.STRING) @Column(name = "premium_frequency", nullable = false, length = 20)
    private PremiumFrequency premiumFrequency;
    @Column(name = "premium_amount", nullable = false, precision = 14, scale = 2)
    private BigDecimal premiumAmount;
    @Column(name = "sum_insured", nullable = false, precision = 16, scale = 2)
    private BigDecimal sumInsured;
    @Column(name = "total_premium_payable", nullable = false, precision = 16, scale = 2)
    private BigDecimal totalPremiumPayable;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20)
    private PolicyStatus status;
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
    @OneToMany(mappedBy = "policy", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("installmentNumber ASC")
    private List<PremiumInstallment> installments = new ArrayList<>();

    public CustomerPolicy(String policyNumber, Customer customer, InsuranceProduct product, LocalDate startDate,
                          LocalDate maturityDate, PremiumFrequency premiumFrequency, BigDecimal premiumAmount,
                          BigDecimal sumInsured, BigDecimal totalPremiumPayable, PolicyStatus status) {
        this.policyNumber = policyNumber;
        this.customer = customer;
        this.product = product;
        this.startDate = startDate;
        this.maturityDate = maturityDate;
        this.premiumFrequency = premiumFrequency;
        this.premiumAmount = premiumAmount;
        this.sumInsured = sumInsured;
        this.totalPremiumPayable = totalPremiumPayable;
        this.status = status;
    }

    @PrePersist
    void onCreate() { createdAt = Instant.now(); }
    public void addInstallment(PremiumInstallment installment) {
        installments.add(installment);
        installment.setPolicy(this);
    }
}
