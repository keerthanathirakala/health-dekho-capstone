package com.healthdekho.repository;

import com.healthdekho.domain.AiRecommendationRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AiRecommendationRequestRepository extends JpaRepository<AiRecommendationRequest, Long> {
    long countByCustomerId(Long customerId);
    @Query("select count(r) from AiRecommendationRequest r where r.customer.id = :customerId")
    long countRequests(@Param("customerId") Long customerId);
}
