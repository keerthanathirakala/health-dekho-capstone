package com.healthdekho.mapper;

import com.healthdekho.domain.*;
import com.healthdekho.dto.*;
import org.springframework.stereotype.Component;

@Component
public class ApiMapper {
    public AuthDtos.CustomerInfo toCustomerInfo(Customer customer) {
        return new AuthDtos.CustomerInfo(customer.getId(), customer.getEmail(), customer.getFullName(),
                customer.getDateOfBirth(), customer.getPhone());
    }

    public ProductDtos.ProductResponse toProduct(InsuranceProduct product) {
        return new ProductDtos.ProductResponse(product.getId(), product.getProductCode(), product.getPolicyName(),
                product.getPolicyType().name(), product.getTermMonths(), product.getPremiumFrequency().name(),
                product.getPremiumAmount(), product.getSumInsured(), product.getMinEntryAge(), product.getMaxEntryAge());
    }

    public PolicyDtos.PolicyResponse toPolicy(CustomerPolicy policy) {
        InsuranceProduct product = policy.getProduct();
        return new PolicyDtos.PolicyResponse(policy.getId(), policy.getPolicyNumber(), product.getProductCode(),
                product.getPolicyName(), product.getPolicyType().name(), product.getTermMonths(),
                policy.getPremiumFrequency().name(), policy.getPremiumAmount(), policy.getSumInsured(),
                policy.getTotalPremiumPayable(), policy.getStatus().name(), policy.getStartDate(), policy.getMaturityDate());
    }
}
