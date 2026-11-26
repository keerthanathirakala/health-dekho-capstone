package com.healthdekho.controller;

import com.healthdekho.dto.ProductDtos;
import com.healthdekho.service.ProductService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/products")
public class ProductController {
    private final ProductService productService;
    public ProductController(ProductService productService) { this.productService = productService; }
    @GetMapping("/available")
    public List<ProductDtos.ProductResponse> available(Authentication authentication) {
        return productService.available(authentication.getName());
    }
}
