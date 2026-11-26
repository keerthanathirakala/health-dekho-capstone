package com.healthdekho.controller;

import com.healthdekho.dto.DashboardDtos;
import com.healthdekho.service.DashboardService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
public class DashboardController {
    private final DashboardService dashboardService;
    public DashboardController(DashboardService dashboardService) { this.dashboardService = dashboardService; }
    @GetMapping("/dashboard")
public DashboardDtos.DashboardResponse dashboard(Authentication authentication) {
    System.out.println("AUTH = " + authentication);
    System.out.println("NAME = " + authentication.getName());
    return dashboardService.get(authentication.getName());
}
}