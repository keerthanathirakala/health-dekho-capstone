package com.healthdekho.domain;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "premium_payment")
@Getter @Setter @NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PremiumPayment {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payment_id")
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "installment_id", nullable = false)
    private PremiumInstallment installment;
    @Column(name = "transaction_reference", nullable = false, unique = true, length = 100)
    private String transactionReference;
    @Column(name = "payment_date", nullable = false)
    private Instant paymentDate;
    @Column(name = "amount_paid", nullable = false, precision = 14, scale = 2)
    private BigDecimal amountPaid;
    @Column(name = "penalty_amount", nullable = false, precision = 14, scale = 2)
    private BigDecimal penaltyAmount = BigDecimal.ZERO;
    @Column(name = "payment_method", nullable = false, length = 30)
    private String paymentMethod;
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    public PremiumPayment(PremiumInstallment installment, String transactionReference, Instant paymentDate,
                          BigDecimal amountPaid, BigDecimal penaltyAmount, String paymentMethod) {
        this.installment = installment;
        this.transactionReference = transactionReference;
        this.paymentDate = paymentDate;
        this.amountPaid = amountPaid;
        this.penaltyAmount = penaltyAmount;
        this.paymentMethod = paymentMethod;
    }

    @PrePersist
    void onCreate() { createdAt = Instant.now(); }
}
