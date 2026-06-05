package com.wasac.billing.repository;

import com.wasac.billing.domain.entity.PenaltyConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Optional;

public interface PenaltyConfigRepository extends JpaRepository<PenaltyConfig, Long> {
    @Query("SELECT p FROM PenaltyConfig p WHERE p.active = true " +
           "AND p.effectiveFrom <= :date AND (p.effectiveTo IS NULL OR p.effectiveTo >= :date) " +
           "ORDER BY p.version DESC")
    Optional<PenaltyConfig> findEffectivePenalty(@Param("date") LocalDate date);
}
