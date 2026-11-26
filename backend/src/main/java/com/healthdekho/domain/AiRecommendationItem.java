package com.healthdekho.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "ai_recommendation_item")
@Getter @Setter @NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AiRecommendationItem {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "recommendation_item_id")
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "recommendation_request_id", nullable = false)
    private AiRecommendationRequest request;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private InsuranceProduct product;
    @Column(name = "rank_order", nullable = false)
    private int rankOrder;
    @Column(name = "recommendation_reason", nullable = false, columnDefinition = "text")
    private String recommendationReason;
    @Column(nullable = false, length = 30)
    private String suitability;
    @Column(name = "selected_for_registration", nullable = false)
    private boolean selectedForRegistration;

    public AiRecommendationItem(InsuranceProduct product, int rankOrder, String recommendationReason, String suitability) {
        this.product = product;
        this.rankOrder = rankOrder;
        this.recommendationReason = recommendationReason;
        this.suitability = suitability;
    }
}
