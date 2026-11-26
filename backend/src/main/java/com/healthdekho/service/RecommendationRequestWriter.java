package com.healthdekho.service;

import com.healthdekho.domain.*;
import com.healthdekho.dto.RecommendationDtos;
import com.healthdekho.repository.AiRecommendationRequestRepository;
import com.healthdekho.repository.CustomerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class RecommendationRequestWriter {
    private final AiRecommendationRequestRepository requests;
    private final CustomerRepository customers;

    public RecommendationRequestWriter(AiRecommendationRequestRepository requests, CustomerRepository customers) {
        this.requests = requests;
        this.customers = customers;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Long create(long customerId, RecommendationDtos.GenerateRequest input, PolicyType policyType,
                       int age, String modelName, String modelVersion, String promptVersion) {
        Customer customer = customers.findById(customerId).orElseThrow();
        return requests.save(new AiRecommendationRequest(customer, input.insuranceGoals(), input.preferredTerm(),
                policyType, age, modelName, modelVersion, promptVersion)).getId();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void addItems(long requestId, List<AiRecommendationItem> items) {
        var request = requests.findById(requestId).orElseThrow();
        items.forEach(request::addItem);
        requests.save(request);
    }
}
