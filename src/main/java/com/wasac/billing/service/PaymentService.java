package com.wasac.billing.service;

import com.wasac.billing.domain.entity.Bill;
import com.wasac.billing.domain.entity.Payment;
import com.wasac.billing.domain.entity.User;
import com.wasac.billing.domain.enums.BillStatus;
import com.wasac.billing.dto.request.PaymentRequest;
import com.wasac.billing.dto.response.PaymentResponse;
import com.wasac.billing.exception.BusinessRuleException;
import com.wasac.billing.exception.ResourceNotFoundException;
import com.wasac.billing.repository.BillRepository;
import com.wasac.billing.repository.PaymentRepository;
import com.wasac.billing.repository.UserRepository;
import com.wasac.billing.security.SecurityHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final BillRepository billRepository;
    private final BillService billService;
    private final UserRepository userRepository;
    private final SecurityHelper securityHelper;

    /**
     * Records a payment (partial or full). Updates outstanding balance and bill status.
     */
    @Transactional
    public PaymentResponse recordPayment(PaymentRequest request) {
        Bill bill = billService.findByReference(request.getBillReference());

        if (bill.getStatus() == BillStatus.PAID) {
            throw new BusinessRuleException("Bill is already fully paid");
        }
        if (bill.getStatus() == BillStatus.CANCELLED) {
            throw new BusinessRuleException("Cannot pay a cancelled bill");
        }
        if (bill.getStatus() == BillStatus.PENDING) {
            throw new BusinessRuleException("Bill must be approved before payment");
        }

        if (request.getAmountPaid().compareTo(bill.getOutstandingBalance()) > 0) {
            throw new BusinessRuleException("Payment amount exceeds outstanding balance");
        }

        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User recorder = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Payment payment = Payment.builder()
                .bill(bill)
                .amountPaid(request.getAmountPaid())
                .paymentMethod(request.getPaymentMethod())
                .paymentDate(request.getPaymentDate())
                .transactionReference(request.getTransactionReference())
                .recordedBy(recorder)
                .build();

        payment = paymentRepository.save(payment);

        // Update bill balances
        BigDecimal newAmountPaid = bill.getAmountPaid().add(request.getAmountPaid());
        BigDecimal newBalance = bill.getOutstandingBalance().subtract(request.getAmountPaid());

        bill.setAmountPaid(newAmountPaid);
        bill.setOutstandingBalance(newBalance);

        if (newBalance.compareTo(BigDecimal.ZERO) == 0) {
            bill.setStatus(BillStatus.PAID);
        }

        billRepository.save(bill);
        // Full-payment notification and status update handled by PostgreSQL trigger (trg_full_payment_notification)

        return PaymentResponse.from(payment);
    }

    @Transactional(readOnly = true)
    public List<PaymentResponse> findByBill(Long billId) {
        Bill bill = billService.getEntityById(billId);
        securityHelper.assertCustomerOwnsData(bill.getCustomer().getId());
        return paymentRepository.findByBillId(billId).stream()
                .map(PaymentResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PaymentResponse> findByCustomer(Long customerId) {
        securityHelper.assertCustomerOwnsData(customerId);
        return paymentRepository.findByBillCustomerId(customerId).stream()
                .map(PaymentResponse::from)
                .toList();
    }
}
