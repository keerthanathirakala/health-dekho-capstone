package com.healthdekho.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "ai_recommendation_request")
@Getter @Setter @NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AiRecommendationRequest {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "recommendation_request_id")
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;
    @Column(name = "insurance_goals", nullable = false, columnDefinition = "text")
    private String insuranceGoals;
    @Column(name = "preferred_term", nullable = false)
    private int preferredTerm;
    @Enumerated(EnumType.STRING) @Column(name = "preferred_policy_type", length = 30)
    private PolicyType preferredPolicyType;
    @Column(name = "calculated_age", nullable = false)
    private int calculatedAge;
    @Column(name = "model_name", nullable = false, length = 100)
    private String modelName;
    @Column(name = "model_version", nullable = false, length = 100)
    private String modelVersion;
    @Column(name = "prompt_version", nullable = false, length = 30)
    private String promptVersion;
    @Column(name = "requested_at", nullable = false)
    private Instant requestedAt;
    @OneToMany(mappedBy = "request", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("rankOrder ASC")
    private List<AiRecommendationItem> items = new ArrayList<>();

    public AiRecommendationRequest(Customer customer, String insuranceGoals, int preferredTerm,
                                   PolicyType preferredPolicyType, int calculatedAge, String modelName,
                                   String modelVersion, String promptVersion) {
        this.customer = customer;
        this.insuranceGoals = insuranceGoals;
        this.preferredTerm = preferredTerm;
        this.preferredPolicyType = preferredPolicyType;
        this.calculatedAge = calculatedAge;
        this.modelName = modelName;
        this.modelVersion = modelVersion;
        this.promptVersion = promptVersion;
    }

    @PrePersist
    void onCreate() { requestedAt = Instant.now(); }
    public void addItem(AiRecommendationItem item) {
        items.add(item);
        item.setRequest(this);
    }
}
