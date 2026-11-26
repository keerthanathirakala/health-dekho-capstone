package com.healthdekho.service;

import com.healthdekho.domain.*;
import com.healthdekho.dto.PageResponse;
import com.healthdekho.dto.PolicyDtos;
import com.healthdekho.exception.ApiException;
import com.healthdekho.mapper.ApiMapper;
import com.healthdekho.repository.CustomerPolicyRepository;
import com.healthdekho.repository.InsuranceProductRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Period;
import java.util.List;

@Service
public class PolicyService {
    private final CustomerAccessService customerAccess;
    private final CustomerPolicyRepository policies;
    private final InsuranceProductRepository products;
    private final ApiMapper mapper;

    public PolicyService(CustomerAccessService customerAccess, CustomerPolicyRepository policies,
                         InsuranceProductRepository products, ApiMapper mapper) {
        this.customerAccess = customerAccess;
        this.policies = policies;
        this.products = products;
        this.mapper = mapper;
    }

    public List<PolicyDtos.PolicyResponse> myPolicies(String email) {
        Customer customer = customerAccess.requireCustomer(email);
        return policies.findAllByCustomerIdOrderByCreatedAtDesc(customer.getId()).stream().map(mapper::toPolicy).toList();
    }

    @Transactional
    public PolicyDtos.RegistrationResponse register(String email, PolicyDtos.RegisterRequest request) {
        Customer customer = customerAccess.requireCustomer(email);
        InsuranceProduct product = products.findById(request.productId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Insurance product was not found"));
        if (!product.isActive()) throw new ApiException(HttpStatus.BAD_REQUEST, "Insurance product is not available");
        if (policies.existsByCustomerIdAndProductId(customer.getId(), product.getId())) {
            throw new ApiException(HttpStatus.CONFLICT, "This product is already registered by the customer");
        }
        LocalDate today = LocalDate.now();
        if (customer.getDateOfBirth().isAfter(today)) throw new ApiException(HttpStatus.BAD_REQUEST, "Date of birth cannot be in the future");
        int age = Period.between(customer.getDateOfBirth(), today).getYears();
        if (age < product.getMinEntryAge() || age > product.getMaxEntryAge()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Customer age is outside the product's eligibility range");
        }
        if (product.getTermMonths() <= 0 || product.getPremiumAmount().compareTo(BigDecimal.ZERO) <= 0
                || product.getSumInsured().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Product premium, sum insured, and term must be positive");
        }
        int frequencyMonths = product.getPremiumFrequency().months();
        if (product.getTermMonths() % frequencyMonths != 0) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Product term must be divisible by its premium frequency");
        }
        int installmentCount = product.getTermMonths() / frequencyMonths;
        BigDecimal total = product.getPremiumAmount().multiply(BigDecimal.valueOf(installmentCount));
        long sequence = policies.nextPolicyNumberSequence();
        String policyNumber = "HDP-" + today.getYear() + "-" + String.format("%04d", sequence);
        CustomerPolicy policy = new CustomerPolicy(policyNumber, customer, product, today,
                today.plusMonths(product.getTermMonths()), product.getPremiumFrequency(), product.getPremiumAmount(),
                product.getSumInsured(), total, PolicyStatus.ACTIVE);
        for (int installment = 1; installment <= installmentCount; installment++) {
            LocalDate dueDate = today.plusMonths((long) (installment - 1) * frequencyMonths);
            policy.addInstallment(new PremiumInstallment(installment, dueDate, dueDate.plusDays(15), product.getPremiumAmount()));
        }
        CustomerPolicy saved = policies.save(policy);
        return new PolicyDtos.RegistrationResponse("Policy registered successfully", mapper.toPolicy(saved));
    }

    public PageResponse<PolicyDtos.PolicyResponse> search(String email, String policyNumber, String policyName,
                                                           PolicyType type, PolicyStatus status, Pageable pageable,
                                                           String sortBy, String direction) {
        Customer customer = customerAccess.requireCustomer(email);
        int page = pageable.getPageNumber();
        int size = pageable.getPageSize();
        if (page < 0 || size < 1 || size > 100) throw new ApiException(HttpStatus.BAD_REQUEST, "Page must be non-negative and size must be between 1 and 100");
        Sort sort = pageable.getSort();
        if (sortBy != null || direction != null) {
            try {
                sort = Sort.by(Sort.Direction.fromString(direction == null ? "desc" : direction),
                        sortBy == null ? "createdAt" : sortBy);
            } catch (IllegalArgumentException ex) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "Sort direction must be asc or desc");
            }
        }
        for (Sort.Order order : sort) {
            if (!List.of("createdAt", "startDate", "maturityDate", "policyNumber", "status", "product.policyName").contains(order.getProperty())) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "Unsupported sort field");
            }
        }
        var result = policies.search(customer.getId(), emptyToNull(policyNumber), emptyToNull(policyName),
                type, status, PageRequest.of(page, size, sort));
        return new PageResponse<>(result.getContent().stream().map(mapper::toPolicy).toList(),
                result.getNumber(), result.getSize(), result.getTotalElements(), result.getTotalPages());
    }

    private String emptyToNull(String value) { return value == null || value.isBlank() ? null : value.trim(); }
}
