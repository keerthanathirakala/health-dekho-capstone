package com.healthdekho.repository;

import com.healthdekho.domain.CustomerPolicy;
import com.healthdekho.domain.PolicyStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface CustomerPolicyRepository extends JpaRepository<CustomerPolicy, Long> {
    @Query(value = "select nextval('policy_number_seq')", nativeQuery = true)
    long nextPolicyNumberSequence();
    @Query("select p from CustomerPolicy p join fetch p.product "
            + "where p.customer.id = :customerId order by p.createdAt desc")
    List<CustomerPolicy> findAllByCustomerIdOrderByCreatedAtDesc(@Param("customerId") Long customerId);
    boolean existsByCustomerIdAndProductId(Long customerId, Long productId);
    Optional<CustomerPolicy> findByIdAndCustomerId(Long id, Long customerId);
    long countByCustomerId(Long customerId);
    long countByCustomerIdAndStatus(Long customerId, PolicyStatus status);
    @Query("""
        select p from CustomerPolicy p join fetch p.product
        where p.customer.id = :customerId
          and (:number is null or lower(p.policyNumber) like lower(concat('%', :number, '%')))
          and (:name is null or lower(p.product.policyName) like lower(concat('%', :name, '%')))
          and (:type is null or p.product.policyType = :type)
          and (:status is null or p.status = :status)
        """)
    Page<CustomerPolicy> search(@Param("customerId") Long customerId,
                                @Param("number") String number,
                                @Param("name") String name,
                                @Param("type") com.healthdekho.domain.PolicyType type,
                                @Param("status") PolicyStatus status,
                                Pageable pageable);
}
