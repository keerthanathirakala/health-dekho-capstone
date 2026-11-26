package com.healthdekho.domain;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "premium_installment")
@Getter @Setter @NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PremiumInstallment {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "installment_id")
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "policy_id", nullable = false)
    private CustomerPolicy policy;
    @Column(name = "installment_number", nullable = false)
    private int installmentNumber;
    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;
    @Column(name = "grace_period_end_date", nullable = false)
    private LocalDate gracePeriodEndDate;
    @Column(name = "scheduled_amount", nullable = false, precision = 14, scale = 2)
    private BigDecimal scheduledAmount;
    @Column(name = "paid_amount", nullable = false, precision = 14, scale = 2)
    private BigDecimal paidAmount = BigDecimal.ZERO;
    @Enumerated(EnumType.STRING) @Column(name = "payment_status", nullable = false, length = 20)
    private PaymentStatus paymentStatus = PaymentStatus.PENDING;
    @OneToMany(mappedBy = "installment", cascade = CascadeType.ALL)
    private List<PremiumPayment> payments = new ArrayList<>();

    public PremiumInstallment(int installmentNumber, LocalDate dueDate, LocalDate gracePeriodEndDate,
                             BigDecimal scheduledAmount) {
        this.installmentNumber = installmentNumber;
        this.dueDate = dueDate;
        this.gracePeriodEndDate = gracePeriodEndDate;
        this.scheduledAmount = scheduledAmount;
    }
}
