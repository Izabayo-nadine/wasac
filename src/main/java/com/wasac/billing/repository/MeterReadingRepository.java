package com.wasac.billing.repository;

import com.wasac.billing.domain.entity.MeterReading;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MeterReadingRepository extends JpaRepository<MeterReading, Long> {
    boolean existsByMeterIdAndBillingMonthAndBillingYear(Long meterId, Integer month, Integer year);
    Optional<MeterReading> findTopByMeterIdOrderByReadingDateDesc(Long meterId);
    List<MeterReading> findByMeterId(Long meterId);
    List<MeterReading> findByBillingMonthAndBillingYear(Integer month, Integer year);
}
