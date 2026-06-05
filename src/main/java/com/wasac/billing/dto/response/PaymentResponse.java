package com.wasac.billing.dto.response;

import com.wasac.billing.domain.entity.Payment;
import com.wasac.billing.domain.enums.PaymentMethod;
import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Value
@Builder
public class PaymentResponse {

    Long id;
    Long billId;
    String billReference;
    BigDecimal amountPaid;
    PaymentMethod paymentMethod;
    LocalDate paymentDate;
    String transactionReference;
    Long recordedByUserId;
    String recordedByName;
    LocalDateTime createdAt;

    public static PaymentResponse from(Payment payment) {
        if (payment == null) {
            return null;
        }
        Long billId = null;
        String billReference = null;
        if (payment.getBill() != null) {
            billId = payment.getBill().getId();
            billReference = payment.getBill().getBillReference();
        }
        Long recordedByUserId = null;
        String recordedByName = null;
        if (payment.getRecordedBy() != null) {
            recordedByUserId = payment.getRecordedBy().getId();
            recordedByName = payment.getRecordedBy().getFullNames();
        }
        return PaymentResponse.builder()
                .id(payment.getId())
                .billId(billId)
                .billReference(billReference)
                .amountPaid(payment.getAmountPaid())
                .paymentMethod(payment.getPaymentMethod())
                .paymentDate(payment.getPaymentDate())
                .transactionReference(payment.getTransactionReference())
                .recordedByUserId(recordedByUserId)
                .recordedByName(recordedByName)
                .createdAt(payment.getCreatedAt())
                .build();
    }
}
