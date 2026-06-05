package com.wasac.billing.repository;

import com.wasac.billing.domain.entity.Tariff;
import com.wasac.billing.domain.enums.MeterType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface TariffRepository extends JpaRepository<Tariff, Long> {
    List<Tariff> findByMeterTypeAndActiveTrue(MeterType meterType);

    /** Picks the highest version whose effective date range includes the billing date (active flag ignored). */
    @Query("SELECT t FROM Tariff t WHERE t.meterType = :meterType " +
           "AND t.effectiveFrom <= :date AND (t.effectiveTo IS NULL OR t.effectiveTo >= :date) " +
           "ORDER BY t.version DESC")
    Optional<Tariff> findEffectiveTariff(@Param("meterType") MeterType meterType, @Param("date") LocalDate date);

    Optional<Tariff> findTopByMeterTypeOrderByVersionDesc(MeterType meterType);
}
