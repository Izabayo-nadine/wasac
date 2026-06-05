package com.wasac.billing.dto.response;

import com.wasac.billing.domain.entity.Bill;
import com.wasac.billing.domain.enums.BillStatus;
import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Value
@Builder
public class BillResponse {

    Long id;
    String billReference;
    Long customerId;
    String customerName;
    Integer billingMonth;
    Integer billingYear;
    BigDecimal consumptionCharge;
    BigDecimal serviceCharge;
    BigDecimal taxAmount;
    BigDecimal penaltyAmount;
    BigDecimal totalAmount;
    BigDecimal amountPaid;
    BigDecimal outstandingBalance;
    BillStatus status;
    LocalDate dueDate;
    LocalDateTime approvedAt;
    Long approvedByUserId;
    String approvedByName;
    List<BillLineItemResponse> lineItems;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;

    public static BillResponse from(Bill bill) {
        if (bill == null) {
            return null;
        }
        Long customerId = null;
        String customerName = null;
        if (bill.getCustomer() != null) {
            customerId = bill.getCustomer().getId();
            customerName = bill.getCustomer().getFullNames();
        }
        Long approvedByUserId = null;
        String approvedByName = null;
        if (bill.getApprovedBy() != null) {
            approvedByUserId = bill.getApprovedBy().getId();
            approvedByName = bill.getApprovedBy().getFullNames();
        }
        List<BillLineItemResponse> lineItems = bill.getLineItems() == null
                ? List.of()
                : bill.getLineItems().stream().map(BillLineItemResponse::from).toList();

        return BillResponse.builder()
                .id(bill.getId())
                .billReference(bill.getBillReference())
                .customerId(customerId)
                .customerName(customerName)
                .billingMonth(bill.getBillingMonth())
                .billingYear(bill.getBillingYear())
                .consumptionCharge(bill.getConsumptionCharge())
                .serviceCharge(bill.getServiceCharge())
                .taxAmount(bill.getTaxAmount())
                .penaltyAmount(bill.getPenaltyAmount())
                .totalAmount(bill.getTotalAmount())
                .amountPaid(bill.getAmountPaid())
                .outstandingBalance(bill.getOutstandingBalance())
                .status(bill.getStatus())
                .dueDate(bill.getDueDate())
                .approvedAt(bill.getApprovedAt())
                .approvedByUserId(approvedByUserId)
                .approvedByName(approvedByName)
                .lineItems(lineItems)
                .createdAt(bill.getCreatedAt())
                .updatedAt(bill.getUpdatedAt())
                .build();
    }
}
