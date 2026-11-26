package com.healthdekho.service;

import com.healthdekho.domain.Customer;
import com.healthdekho.domain.CustomerPolicy;
import com.healthdekho.domain.InsuranceProduct;
import com.healthdekho.domain.PaymentStatus;
import com.healthdekho.domain.PolicyStatus;
import com.healthdekho.domain.PolicyType;
import com.healthdekho.domain.PremiumFrequency;
import com.healthdekho.domain.PremiumInstallment;
import com.healthdekho.domain.PremiumPayment;
import com.healthdekho.dto.PremiumDtos;
import com.healthdekho.exception.ApiException;
import com.healthdekho.repository.CustomerPolicyRepository;
import com.healthdekho.repository.PremiumInstallmentRepository;
import com.healthdekho.repository.PremiumPaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PremiumServiceTest {
    @Mock private CustomerAccessService customerAccess;
    @Mock private CustomerPolicyRepository policies;
    @Mock private PremiumInstallmentRepository installments;
    @Mock private PremiumPaymentRepository payments;

    private PremiumService service;
    private PremiumInstallment installment;

    @BeforeEach
    void setUp() {
        service = new PremiumService(customerAccess, policies, installments, payments);
        Customer customer = new Customer("test@example.com", "hash", "Test Customer",
                LocalDate.of(1990, 1, 1), null);
        customer.setId(1001L);
        InsuranceProduct product = new InsuranceProduct("HD-TERM-001", "Term Cover", PolicyType.TERM,
                12, PremiumFrequency.MONTHLY, new BigDecimal("100.00"), new BigDecimal("10000.00"),
                18, 60);
        CustomerPolicy policy = new CustomerPolicy("HDP-2026-0001", customer, product,
                LocalDate.now(), LocalDate.now().plusYears(1), PremiumFrequency.MONTHLY,
                new BigDecimal("100.00"), new BigDecimal("10000.00"), new BigDecimal("1200.00"),
                PolicyStatus.ACTIVE);
        installment = new PremiumInstallment(1, LocalDate.now(), LocalDate.now().plusDays(15),
                new BigDecimal("100.00"));
        installment.setId(22L);
        installment.setPolicy(policy);
        installment.setPaidAmount(BigDecimal.ZERO);
        installment.setPaymentStatus(PaymentStatus.PENDING);

        when(customerAccess.requireCustomer("test@example.com")).thenReturn(customer);
        when(installments.findOwnedByIdForUpdate(22L, 1001L)).thenReturn(Optional.of(installment));
    }

    @Test
    void recordsPartialPaymentAndLeavesInstallmentPending() {
        when(payments.save(any(PremiumPayment.class))).thenAnswer(invocation -> invocation.getArgument(0));
        PremiumDtos.PaymentResponse response = service.pay("test@example.com",
                new PremiumDtos.PayRequest(22L, new BigDecimal("40.00"), " UPI "));

        assertEquals(new BigDecimal("40.00"), installment.getPaidAmount());
        assertEquals(PaymentStatus.PENDING, installment.getPaymentStatus());
        assertEquals(new BigDecimal("40.00"), response.amountPaid());
        assertEquals("UPI", response.paymentMethod());
        verify(payments).save(any(PremiumPayment.class));
    }

    @Test
    void marksInstallmentPaidWhenRemainingBalanceIsPaid() {
        when(payments.save(any(PremiumPayment.class))).thenAnswer(invocation -> invocation.getArgument(0));
        service.pay("test@example.com", new PremiumDtos.PayRequest(22L, new BigDecimal("100.00"), "CARD"));

        assertEquals(new BigDecimal("100.00"), installment.getPaidAmount());
        assertEquals(PaymentStatus.PAID, installment.getPaymentStatus());
    }

    @Test
    void rejectsPaymentAboveRemainingBalance() {
        assertThrows(ApiException.class, () -> service.pay("test@example.com",
                new PremiumDtos.PayRequest(22L, new BigDecimal("100.01"), "CARD")));

        assertEquals(BigDecimal.ZERO, installment.getPaidAmount());
        verifyNoInteractions(payments);
    }

    @Test
    void gracePeriodEndIsNotLate() {
        installment.setGracePeriodEndDate(LocalDate.now());
        when(payments.save(any(PremiumPayment.class))).thenAnswer(invocation -> invocation.getArgument(0));
        var response = service.pay("test@example.com", new PremiumDtos.PayRequest(22L, new BigDecimal("40.00"), "UPI"));
        assertEquals(0, response.penaltyAmount().compareTo(BigDecimal.ZERO));
        assertEquals(PaymentStatus.PENDING, installment.getPaymentStatus());
    }

    @Test
    void firstLatePartialPaymentChargesRoundedPenaltyOnScheduledAmount() {
        installment.setGracePeriodEndDate(LocalDate.now().minusDays(1));
        installment.setScheduledAmount(new BigDecimal("100.25"));
        when(payments.save(any(PremiumPayment.class))).thenAnswer(invocation -> invocation.getArgument(0));
        var response = service.pay("test@example.com", new PremiumDtos.PayRequest(22L, new BigDecimal("40.00"), "UPI"));
        assertEquals(new BigDecimal("2.01"), response.penaltyAmount());
        assertEquals(new BigDecimal("40.00"), installment.getPaidAmount());
        assertEquals(PaymentStatus.OVERDUE, installment.getPaymentStatus());
    }

    @Test
    void subsequentLatePaymentDoesNotChargePenaltyAgain() {
        installment.setGracePeriodEndDate(LocalDate.now().minusDays(1));
        installment.setPaidAmount(new BigDecimal("40.00"));
        when(payments.existsByInstallmentIdAndPenaltyAmountGreaterThan(22L, BigDecimal.ZERO)).thenReturn(true);
        when(payments.save(any(PremiumPayment.class))).thenAnswer(invocation -> invocation.getArgument(0));
        var response = service.pay("test@example.com", new PremiumDtos.PayRequest(22L, new BigDecimal("60.00"), "UPI"));
        assertEquals(0, response.penaltyAmount().compareTo(BigDecimal.ZERO));
        assertEquals(PaymentStatus.PAID, installment.getPaymentStatus());
    }
}
