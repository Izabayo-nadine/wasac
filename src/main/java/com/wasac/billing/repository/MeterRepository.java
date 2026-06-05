package com.wasac.billing.repository;

import com.wasac.billing.domain.entity.Meter;
import com.wasac.billing.domain.enums.MeterStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MeterRepository extends JpaRepository<Meter, Long> {
    Optional<Meter> findByMeterNumber(String meterNumber);
    boolean existsByMeterNumber(String meterNumber);
    List<Meter> findByCustomerId(Long customerId);
    List<Meter> findByCustomerIdAndStatus(Long customerId, MeterStatus status);
}
