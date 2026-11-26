package com.healthdekho.service;

import com.healthdekho.domain.PolicyStatus;
import com.healthdekho.domain.PaymentStatus;
import com.healthdekho.dto.DashboardDtos;
import com.healthdekho.dto.PolicyDtos;
import com.healthdekho.mapper.ApiMapper;
import com.healthdekho.repository.AiRecommendationRequestRepository;
import com.healthdekho.repository.CustomerPolicyRepository;
import com.healthdekho.repository.PremiumInstallmentRepository;
import org.springframework.stereotype.Service;

@Service
public class DashboardService {
    private final CustomerAccessService customerAccess;
    private final ProductService productService;
    private final CustomerPolicyRepository policies;
    private final PremiumInstallmentRepository installments;
    private final AiRecommendationRequestRepository requests;
    private final ApiMapper mapper;

    public DashboardService(CustomerAccessService customerAccess, ProductService productService,
                            CustomerPolicyRepository policies, PremiumInstallmentRepository installments,
                            AiRecommendationRequestRepository requests, ApiMapper mapper) {
        this.customerAccess = customerAccess;
        this.productService = productService;
        this.policies = policies;
        this.installments = installments;
        this.requests = requests;
        this.mapper = mapper;
    }

    public DashboardDtos.DashboardResponse get(String email) {
        var customer = customerAccess.requireCustomer(email);
        var registered = policies.findAllByCustomerIdOrderByCreatedAtDesc(customer.getId()).stream()
                .map(mapper::toPolicy).toList();
        var stats = new DashboardDtos.Statistics(policies.countByCustomerId(customer.getId()),
                policies.countByCustomerIdAndStatus(customer.getId(), PolicyStatus.ACTIVE),
                installments.countByPolicyCustomerIdAndPaymentStatus(customer.getId(), PaymentStatus.PENDING),
                requests.countRequests(customer.getId()));
        return new DashboardDtos.DashboardResponse(mapper.toCustomerInfo(customer), registered,
                productService.eligibleProducts(customer, customer.getDateOfBirth()).stream().map(mapper::toProduct).toList(),
                stats);
    }
}
