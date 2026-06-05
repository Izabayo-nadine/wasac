package com.wasac.billing.controller;

import com.wasac.billing.dto.response.ApiResponse;
import com.wasac.billing.dto.response.NotificationResponse;
import com.wasac.billing.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Tag(name = "Notifications", description = "Customer notification messages")
@SecurityRequirement(name = "bearerAuth")
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE')")
    @Operation(summary = "List all notifications")
    public ApiResponse<List<NotificationResponse>> findAll() {
        return ApiResponse.ok(notificationService.findAll());
    }

    @GetMapping("/customer/{customerId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE', 'CUSTOMER')")
    @Operation(summary = "List notifications for a customer")
    public ApiResponse<List<NotificationResponse>> findByCustomer(@PathVariable Long customerId) {
        return ApiResponse.ok(notificationService.findByCustomer(customerId));
    }
}
