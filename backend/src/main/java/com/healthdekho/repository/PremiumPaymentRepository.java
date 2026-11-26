package com.healthdekho.repository;

import com.healthdekho.domain.PremiumPayment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface PremiumPaymentRepository extends JpaRepository<PremiumPayment, Long> {
    boolean existsByInstallmentIdAndPenaltyAmountGreaterThan(Long installmentId, java.math.BigDecimal amount);
    @Query("""
        select payment from PremiumPayment payment
        join fetch payment.installment installment
        join fetch installment.policy policy
        join fetch policy.product
        where policy.customer.id = :customerId
        order by payment.paymentDate desc
        """)
    List<PremiumPayment> findHistoryByCustomerId(@Param("customerId") Long customerId);
    @Query("""
        select payment from PremiumPayment payment
        join fetch payment.installment installment
        join fetch installment.policy policy
        join fetch policy.product
        where policy.id = :policyId and policy.customer.id = :customerId
        order by payment.paymentDate desc
        """)
    List<PremiumPayment> findByPolicyAndCustomer(@Param("policyId") Long policyId,
                                                 @Param("customerId") Long customerId);
}
