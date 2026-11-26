package com.healthdekho.service;

import com.healthdekho.domain.Customer;
import com.healthdekho.domain.InsuranceProduct;
import com.healthdekho.dto.ProductDtos;
import com.healthdekho.mapper.ApiMapper;
import com.healthdekho.repository.CustomerPolicyRepository;
import com.healthdekho.repository.InsuranceProductRepository;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.Period;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.List;

@Service
public class ProductService {
    private final InsuranceProductRepository products;
    private final CustomerPolicyRepository policies;
    private final CustomerAccessService customerAccess;
    private final ApiMapper mapper;

    public ProductService(InsuranceProductRepository products, CustomerPolicyRepository policies,
                         CustomerAccessService customerAccess, ApiMapper mapper) {
        this.products = products;
        this.policies = policies;
        this.customerAccess = customerAccess;
        this.mapper = mapper;
    }

    public List<InsuranceProduct> eligibleProducts(Customer customer, LocalDate dateOfBirth) {
        int age = Period.between(dateOfBirth, LocalDate.now()).getYears();
        Set<Long> registeredIds = policies.findAllByCustomerIdOrderByCreatedAtDesc(customer.getId()).stream()
                .map(policy -> policy.getProduct().getId()).collect(Collectors.toSet());
        return products.findAllByActiveTrueOrderByProductCodeAsc().stream()
                .filter(product -> !registeredIds.contains(product.getId()))
                .filter(product -> age >= product.getMinEntryAge() && age <= product.getMaxEntryAge())
                .toList();
    }

    public List<ProductDtos.ProductResponse> available(String email) {
        Customer customer = customerAccess.requireCustomer(email);
        return eligibleProducts(customer, customer.getDateOfBirth()).stream().map(mapper::toProduct).toList();
    }
}
