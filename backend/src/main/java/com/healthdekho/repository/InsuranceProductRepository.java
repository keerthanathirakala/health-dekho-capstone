package com.healthdekho.repository;

import com.healthdekho.domain.InsuranceProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface InsuranceProductRepository extends JpaRepository<InsuranceProduct, Long> {
    List<InsuranceProduct> findAllByActiveTrueOrderByProductCodeAsc();
}
