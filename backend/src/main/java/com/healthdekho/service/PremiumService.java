package com.healthdekho.service;

import com.healthdekho.domain.PaymentStatus;
import com.healthdekho.domain.PremiumPayment;
import com.healthdekho.dto.PremiumDtos;
import com.healthdekho.exception.ApiException;
import com.healthdekho.repository.CustomerPolicyRepository;
import com.healthdekho.repository.PremiumInstallmentRepository;
import com.healthdekho.repository.PremiumPaymentRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class PremiumService {
    private final CustomerAccessService customerAccess;
    private final CustomerPolicyRepository policies;
    private final PremiumInstallmentRepository installments;
    private final PremiumPaymentRepository payments;

    public PremiumService(CustomerAccessService customerAccess, CustomerPolicyRepository policies,
                         PremiumInstallmentRepository installments, PremiumPaymentRepository payments) {
        this.customerAccess = customerAccess;
        this.policies = policies;
        this.installments = installments;
        this.payments = payments;
    }

    @Transactional
    public PremiumDtos.PaymentResponse pay(String email, PremiumDtos.PayRequest request) {
        var customer = customerAccess.requireCustomer(email);
        var installment = installments.findOwnedByIdForUpdate(request.installmentId(), customer.getId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Installment was not found"));
        BigDecimal remaining = installment.getScheduledAmount().subtract(installment.getPaidAmount());
        if (remaining.signum() <= 0 || installment.getPaymentStatus() == PaymentStatus.PAID) {
            throw new ApiException(HttpStatus.CONFLICT, "Installment is already fully paid");
        }
        if (request.amount().compareTo(remaining) > 0) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Payment amount exceeds the remaining installment balance");
        }

        LocalDate paymentDate = LocalDate.now();
        BigDecimal penalty = BigDecimal.ZERO;
        if (paymentDate.isAfter(installment.getGracePeriodEndDate())
                && !payments.existsByInstallmentIdAndPenaltyAmountGreaterThan(installment.getId(), BigDecimal.ZERO)) {
            penalty = installment.getScheduledAmount().multiply(new BigDecimal("0.02"))
                    .setScale(2, RoundingMode.HALF_UP);
        }
        BigDecimal paidAmount = installment.getPaidAmount().add(request.amount());
        installment.setPaidAmount(paidAmount);
        if (paidAmount.compareTo(installment.getScheduledAmount()) >= 0) {
            installment.setPaymentStatus(PaymentStatus.PAID);
        } else if (installment.getPaymentStatus() == PaymentStatus.OVERDUE
                || paymentDate.isAfter(installment.getGracePeriodEndDate())) {
            installment.setPaymentStatus(PaymentStatus.OVERDUE);
        } else {
            installment.setPaymentStatus(PaymentStatus.PENDING);
        }

        PremiumPayment payment = new PremiumPayment(installment, "PAY-" + UUID.randomUUID(),
                Instant.now(), request.amount(), penalty, request.paymentMethod().trim());
        return toPayment(payments.save(payment));
    }

    public List<PremiumDtos.InstallmentResponse> installments(String email, long policyId) {
        var customer = customerAccess.requireCustomer(email);
        var policy = policies.findByIdAndCustomerId(policyId, customer.getId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Policy was not found"));
        return installments.findAllByPolicyIdOrderByInstallmentNumberAsc(policy.getId()).stream()
                .map(item -> new PremiumDtos.InstallmentResponse(item.getId(), policy.getPolicyNumber(),
                        item.getInstallmentNumber(), item.getDueDate(), item.getGracePeriodEndDate(),
                        item.getScheduledAmount(), item.getPaidAmount(), item.getPaymentStatus().name()))
                .toList();
    }

    public List<PremiumDtos.PaymentResponse> history(String email) {
        var customer = customerAccess.requireCustomer(email);
        return payments.findHistoryByCustomerId(customer.getId()).stream().map(this::toPayment).toList();
    }

    public List<PremiumDtos.PaymentResponse> paymentsForPolicy(String email, long policyId) {
        var customer = customerAccess.requireCustomer(email);
        if (policies.findByIdAndCustomerId(policyId, customer.getId()).isEmpty()) {
            throw new ApiException(HttpStatus.NOT_FOUND, "Policy was not found");
        }
        return payments.findByPolicyAndCustomer(policyId, customer.getId()).stream().map(this::toPayment).toList();
    }

    private PremiumDtos.PaymentResponse toPayment(com.healthdekho.domain.PremiumPayment payment) {
        var installment = payment.getInstallment();
        var policy = installment.getPolicy();
        return new PremiumDtos.PaymentResponse(payment.getId(), policy.getPolicyNumber(),
                policy.getProduct().getPolicyName(), installment.getInstallmentNumber(),
                payment.getTransactionReference(), payment.getPaymentDate(), payment.getAmountPaid(),
                payment.getPenaltyAmount(), payment.getPaymentMethod());
    }
}
