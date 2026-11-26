package com.healthdekho.controller;

import com.healthdekho.domain.PolicyStatus;
import com.healthdekho.domain.PolicyType;
import com.healthdekho.dto.PageResponse;
import com.healthdekho.dto.PolicyDtos;
import com.healthdekho.service.PolicyService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/policies")
public class PolicyController {
    private final PolicyService policyService;
    public PolicyController(PolicyService policyService) { this.policyService = policyService; }

    @GetMapping("/my")
    public List<PolicyDtos.PolicyResponse> myPolicies(Authentication authentication) {
        return policyService.myPolicies(authentication.getName());
    }

    @PostMapping("/register")
    public PolicyDtos.RegistrationResponse register(Authentication authentication,
                                                     @Valid @RequestBody PolicyDtos.RegisterRequest request) {
        return policyService.register(authentication.getName(), request);
    }

    @GetMapping("/search")
    public PageResponse<PolicyDtos.PolicyResponse> search(Authentication authentication,
            @RequestParam(required = false) String policyNumber,
            @RequestParam(required = false) String policyName,
            @RequestParam(required = false) PolicyType policyType,
            @RequestParam(required = false) PolicyStatus status,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String direction) {
        return policyService.search(authentication.getName(), policyNumber, policyName, policyType,
                status, pageable, sortBy, direction);
    }
}
