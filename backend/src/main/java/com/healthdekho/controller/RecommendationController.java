package com.healthdekho.controller;

import com.healthdekho.dto.RecommendationDtos;
import com.healthdekho.service.RecommendationService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/recommendations")
public class RecommendationController {
    private final RecommendationService recommendationService;
    public RecommendationController(RecommendationService recommendationService) {
        this.recommendationService = recommendationService;
    }
    @PostMapping("/generate")
    public RecommendationDtos.RecommendationResponse generate(Authentication authentication,
            @Valid @RequestBody RecommendationDtos.GenerateRequest request) {
        return recommendationService.generate(authentication.getName(), request);
    }
}
