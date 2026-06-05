package com.wasac.billing.repository;

import com.wasac.billing.domain.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer, Long> {
    boolean existsByNationalId(String nationalId);
    boolean existsByEmail(String email);
    Optional<Customer> findByNationalId(String nationalId);
    Optional<Customer> findByUserId(Long userId);
}
