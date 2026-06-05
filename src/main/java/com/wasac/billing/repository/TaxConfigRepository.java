package com.wasac.billing.repository;

import com.wasac.billing.domain.entity.TaxConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Optional;

public interface TaxConfigRepository extends JpaRepository<TaxConfig, Long> {
    @Query("SELECT t FROM TaxConfig t WHERE t.effectiveFrom <= :date " +
           "AND (t.effectiveTo IS NULL OR t.effectiveTo >= :date) " +
           "ORDER BY t.version DESC")
    Optional<TaxConfig> findEffectiveTax(@Param("date") LocalDate date);
}
