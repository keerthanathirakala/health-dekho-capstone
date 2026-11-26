package com.healthdekho.dto;

import java.util.List;

public final class DashboardDtos {
    private DashboardDtos() {}
    public record Statistics(long totalPolicies, long activePolicies, long pendingInstallments,
                             long recommendationsGenerated) {}
    public record DashboardResponse(AuthDtos.CustomerInfo customer, List<PolicyDtos.PolicyResponse> registeredPolicies,
                                    List<ProductDtos.ProductResponse> availablePolicies, Statistics statistics) {}
}
