package com.wasac.billing.controller;

import com.wasac.billing.dto.request.PaymentRequest;
import com.wasac.billing.dto.response.ApiResponse;
import com.wasac.billing.dto.response.PaymentResponse;
import com.wasac.billing.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Tag(name = "Payments", description = "Payment recording and history")
@SecurityRequirement(name = "bearerAuth")
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE')")
    @Operation(summary = "Record a partial or full payment")
    public ApiResponse<PaymentResponse> record(@Valid @RequestBody PaymentRequest request) {
        return ApiResponse.ok("Payment recorded", paymentService.recordPayment(request));
    }

    @GetMapping("/bill/{billId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE', 'CUSTOMER')")
    @Operation(summary = "List payments for a bill")
    public ApiResponse<List<PaymentResponse>> findByBill(@PathVariable Long billId) {
        return ApiResponse.ok(paymentService.findByBill(billId));
    }

    @GetMapping("/customer/{customerId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE', 'CUSTOMER')")
    @Operation(summary = "List payment history for a customer")
    public ApiResponse<List<PaymentResponse>> findByCustomer(@PathVariable Long customerId) {
        return ApiResponse.ok(paymentService.findByCustomer(customerId));
    }
}
