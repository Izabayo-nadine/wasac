package com.wasac.billing.dto.response;

import com.wasac.billing.domain.entity.Notification;
import com.wasac.billing.domain.enums.NotificationType;
import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;

@Value
@Builder
public class NotificationResponse {

    Long id;
    Long customerId;
    String customerName;
    Long billId;
    String billReference;
    NotificationType type;
    String message;
    boolean sent;
    LocalDateTime createdAt;

    public static NotificationResponse from(Notification notification) {
        if (notification == null) {
            return null;
        }
        Long customerId = null;
        String customerName = null;
        if (notification.getCustomer() != null) {
            customerId = notification.getCustomer().getId();
            customerName = notification.getCustomer().getFullNames();
        }
        Long billId = null;
        String billReference = null;
        if (notification.getBill() != null) {
            billId = notification.getBill().getId();
            billReference = notification.getBill().getBillReference();
        }
        return NotificationResponse.builder()
                .id(notification.getId())
                .customerId(customerId)
                .customerName(customerName)
                .billId(billId)
                .billReference(billReference)
                .type(notification.getType())
                .message(notification.getMessage())
                .sent(Boolean.TRUE.equals(notification.getSent()))
                .createdAt(notification.getCreatedAt())
                .build();
    }
}
