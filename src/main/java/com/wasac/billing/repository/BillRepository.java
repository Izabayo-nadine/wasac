package com.wasac.billing.repository;

import com.wasac.billing.domain.entity.Bill;
import com.wasac.billing.domain.enums.BillStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BillRepository extends JpaRepository<Bill, Long> {
    Optional<Bill> findByBillReference(String billReference);
    boolean existsByCustomerIdAndBillingMonthAndBillingYear(Long customerId, Integer month, Integer year);
    List<Bill> findByCustomerId(Long customerId);
    List<Bill> findByStatus(BillStatus status);
}
