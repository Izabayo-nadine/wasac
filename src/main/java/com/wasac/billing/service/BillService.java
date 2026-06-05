package com.wasac.billing.service;

import com.wasac.billing.domain.entity.*;
import com.wasac.billing.domain.enums.BillStatus;
import com.wasac.billing.domain.enums.CustomerStatus;
import com.wasac.billing.domain.enums.MeterStatus;
import com.wasac.billing.dto.request.BillGenerationRequest;
import com.wasac.billing.dto.response.BillResponse;
import com.wasac.billing.exception.BusinessRuleException;
import com.wasac.billing.exception.DuplicateResourceException;
import com.wasac.billing.exception.ResourceNotFoundException;
import com.wasac.billing.repository.BillRepository;
import com.wasac.billing.repository.CustomerRepository;
import com.wasac.billing.repository.MeterReadingRepository;
import com.wasac.billing.repository.UserRepository;
import com.wasac.billing.security.SecurityHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BillService {

    private final BillRepository billRepository;
    private final CustomerRepository customerRepository;
    private final MeterReadingRepository readingRepository;
    private final TariffService tariffService;
    private final TaxPenaltyService taxPenaltyService;
    private final UserRepository userRepository;
    private final SecurityHelper securityHelper;

    @Transactional
    public Bill generateBill(Customer customer, BillGenerationRequest request) {
        if (customer.getStatus() == CustomerStatus.INACTIVE) {
            throw new BusinessRuleException("Inactive customers cannot receive bills");
        }

        if (billRepository.existsByCustomerIdAndBillingMonthAndBillingYear(
                customer.getId(), request.getBillingMonth(), request.getBillingYear())) {
            throw new DuplicateResourceException("Bill already exists for this customer and period");
        }

        LocalDate billingDate = LocalDate.of(request.getBillingYear(), request.getBillingMonth(), 1);
        BigDecimal totalConsumptionCharge = BigDecimal.ZERO;
        BigDecimal totalServiceCharge = BigDecimal.ZERO;
        List<BillLineItem> lineItems = new ArrayList<>();

        for (Meter meter : customer.getMeters()) {
            if (meter.getStatus() != MeterStatus.ACTIVE) continue;

            List<MeterReading> readings = readingRepository.findByMeterId(meter.getId()).stream()
                    .filter(r -> r.getBillingMonth().equals(request.getBillingMonth())
                            && r.getBillingYear().equals(request.getBillingYear()))
                    .toList();

            if (readings.isEmpty()) continue;

            MeterReading reading = readings.get(0);
            BigDecimal consumption = reading.getConsumption();
            Tariff tariff = tariffService.findEffective(meter.getMeterType(), billingDate);

            BigDecimal consumptionCharge = tariffService.calculateConsumptionCharge(tariff, consumption);
            totalConsumptionCharge = totalConsumptionCharge.add(consumptionCharge);
            totalServiceCharge = totalServiceCharge.add(tariff.getServiceCharge());

            lineItems.add(BillLineItem.builder()
                    .meter(meter)
                    .meterType(meter.getMeterType())
                    .consumption(consumption)
                    .amount(consumptionCharge.add(tariff.getServiceCharge()))
                    .description(meter.getMeterType() + " - " + meter.getMeterNumber())
                    .build());
        }

        if (lineItems.isEmpty()) {
            throw new BusinessRuleException("No meter readings found for billing period");
        }

        BigDecimal subtotal = totalConsumptionCharge.add(totalServiceCharge);
        BigDecimal taxAmount = BigDecimal.ZERO;
        TaxConfig tax = taxPenaltyService.findEffectiveTax(billingDate);
        if (tax != null) {
            taxAmount = subtotal.multiply(tax.getRate())
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        }

        BigDecimal totalAmount = subtotal.add(taxAmount);

        Bill bill = Bill.builder()
                .billReference(generateBillReference())
                .customer(customer)
                .billingMonth(request.getBillingMonth())
                .billingYear(request.getBillingYear())
                .consumptionCharge(totalConsumptionCharge)
                .serviceCharge(totalServiceCharge)
                .taxAmount(taxAmount)
                .penaltyAmount(BigDecimal.ZERO)
                .totalAmount(totalAmount)
                .amountPaid(BigDecimal.ZERO)
                .outstandingBalance(totalAmount)
                .status(BillStatus.PENDING)
                .dueDate(billingDate.plusDays(30))
                .build();

        for (BillLineItem item : lineItems) {
            item.setBill(bill);
            bill.getLineItems().add(item);
        }

        // Notification inserted by PostgreSQL trigger (trg_bill_notification)
        return billRepository.save(bill);
    }

    @Transactional
    public List<BillResponse> generateBills(BillGenerationRequest request) {
        List<Customer> customers;
        if (request.getCustomerId() != null) {
            customers = List.of(customerRepository.findById(request.getCustomerId())
                    .orElseThrow(() -> new ResourceNotFoundException("Customer not found")));
        } else {
            customers = customerRepository.findAll().stream()
                    .filter(c -> c.getStatus() == CustomerStatus.ACTIVE)
                    .toList();
        }

        List<BillResponse> bills = new ArrayList<>();
        for (Customer customer : customers) {
            try {
                bills.add(BillResponse.from(generateBill(customer, request)));
            } catch (BusinessRuleException | DuplicateResourceException ex) {
                // Skip customers without readings or with existing bills
            }
        }
        return bills;
    }

    @Transactional
    public BillResponse approveBill(Long billId) {
        Bill bill = getEntityById(billId);
        if (bill.getStatus() != BillStatus.PENDING) {
            throw new BusinessRuleException("Only PENDING bills can be approved");
        }

        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User approver = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        bill.setStatus(BillStatus.APPROVED);
        bill.setApprovedAt(LocalDateTime.now());
        bill.setApprovedBy(approver);

        return BillResponse.from(billRepository.save(bill));
    }

    @Transactional(readOnly = true)
    public BillResponse findById(Long id) {
        Bill bill = getEntityById(id);
        securityHelper.assertCustomerOwnsData(bill.getCustomer().getId());
        return BillResponse.from(bill);
    }

    @Transactional(readOnly = true)
    public Bill getEntityById(Long id) {
        return billRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bill not found: " + id));
    }

    @Transactional(readOnly = true)
    public Bill findByReference(String reference) {
        return billRepository.findByBillReference(reference)
                .orElseThrow(() -> new ResourceNotFoundException("Bill not found: " + reference));
    }

    @Transactional(readOnly = true)
    public List<BillResponse> findByCustomer(Long customerId) {
        securityHelper.assertCustomerOwnsData(customerId);
        return billRepository.findByCustomerId(customerId).stream()
                .map(BillResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<BillResponse> findAll() {
        return billRepository.findAll().stream()
                .map(BillResponse::from)
                .toList();
    }

    private String generateBillReference() {
        return "BILL-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
