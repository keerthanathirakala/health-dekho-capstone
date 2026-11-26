package com.healthdekho.controller;

import com.healthdekho.dto.PremiumDtos;
import com.healthdekho.service.PremiumService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
public class PremiumController {
    private final PremiumService premiumService;
    public PremiumController(PremiumService premiumService) { this.premiumService = premiumService; }

    @PostMapping("/payments/pay")
    public PremiumDtos.PaymentResponse pay(Authentication authentication,
                                            @Valid @RequestBody PremiumDtos.PayRequest request) {
        return premiumService.pay(authentication.getName(), request);
    }

    @GetMapping("/installments/policy/{policyId}")
    public List<PremiumDtos.InstallmentResponse> installments(Authentication authentication, @PathVariable long policyId) {
        return premiumService.installments(authentication.getName(), policyId);
    }

    @GetMapping("/payments/policy/{policyId}")
    public List<PremiumDtos.PaymentResponse> policyPayments(Authentication authentication, @PathVariable long policyId) {
        return premiumService.paymentsForPolicy(authentication.getName(), policyId);
    }

    @GetMapping("/payments/history")
    public List<PremiumDtos.PaymentResponse> paymentHistory(Authentication authentication) {
        return premiumService.history(authentication.getName());
    }
}
