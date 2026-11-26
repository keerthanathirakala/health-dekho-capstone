package com.healthdekho.repository;

import com.healthdekho.domain.PremiumInstallment;
import com.healthdekho.domain.PaymentStatus;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;

public interface PremiumInstallmentRepository extends JpaRepository<PremiumInstallment, Long> {
    List<PremiumInstallment> findAllByPolicyIdOrderByInstallmentNumberAsc(Long policyId);
    long countByPolicyCustomerIdAndPaymentStatus(Long customerId, PaymentStatus status);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select i from PremiumInstallment i join i.policy p "
            + "where i.id = :installmentId and p.customer.id = :customerId")
    Optional<PremiumInstallment> findOwnedByIdForUpdate(@Param("installmentId") Long installmentId,
                                                        @Param("customerId") Long customerId);
}
