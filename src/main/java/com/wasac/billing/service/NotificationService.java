package com.wasac.billing.service;

import com.wasac.billing.domain.entity.Bill;
import com.wasac.billing.domain.entity.Customer;
import com.wasac.billing.domain.entity.Notification;
import com.wasac.billing.domain.enums.NotificationType;
import com.wasac.billing.dto.response.NotificationResponse;
import com.wasac.billing.repository.NotificationRepository;
import com.wasac.billing.security.SecurityHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final SecurityHelper securityHelper;

    /**
     * Message format per requirements:
     * "Dear <name>, Your utility bill of <amount> FRW has been successfully processed."
     */
    @Transactional
    public Notification createBillNotification(Customer customer, Bill bill) {
        String message = String.format(
                "Dear %s, Your utility bill of %s FRW has been successfully processed.",
                customer.getFullNames(),
                bill.getTotalAmount()
        );

        Notification notification = Notification.builder()
                .customer(customer)
                .bill(bill)
                .type(NotificationType.BILL_GENERATED)
                .message(message)
                .sent(false)
                .build();

        return notificationRepository.save(notification);
    }

    @Transactional
    public Notification createPaymentNotification(Customer customer, Bill bill, boolean fullyPaid) {
        String message;
        if (fullyPaid) {
            message = String.format(
                    "Dear %s, Your utility bill of %s FRW has been successfully processed.",
                    customer.getFullNames(),
                    bill.getTotalAmount()
            );
        } else {
            message = String.format(
                    "Dear %s, A partial payment of %s FRW has been received. Outstanding balance: %s FRW.",
                    customer.getFullNames(),
                    bill.getAmountPaid(),
                    bill.getOutstandingBalance()
            );
        }

        Notification notification = Notification.builder()
                .customer(customer)
                .bill(bill)
                .type(fullyPaid ? NotificationType.BILL_PAID : NotificationType.PAYMENT_RECEIVED)
                .message(message)
                .sent(false)
                .build();

        return notificationRepository.save(notification);
    }

    @Transactional(readOnly = true)
    public List<NotificationResponse> findByCustomer(Long customerId) {
        securityHelper.assertCustomerOwnsData(customerId);
        return notificationRepository.findByCustomerIdOrderByCreatedAtDesc(customerId).stream()
                .map(NotificationResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<NotificationResponse> findAll() {
        return notificationRepository.findAll().stream()
                .map(NotificationResponse::from)
                .toList();
    }
}
