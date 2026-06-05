package com.wasac.billing.controller;

import com.wasac.billing.domain.enums.MeterStatus;
import com.wasac.billing.dto.request.MeterRequest;
import com.wasac.billing.dto.response.ApiResponse;
import com.wasac.billing.dto.response.MeterResponse;
import com.wasac.billing.service.MeterService;
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
@RequestMapping("/api/meters")
@RequiredArgsConstructor
@Tag(name = "Meters", description = "Meter management")
@SecurityRequirement(name = "bearerAuth")
public class MeterController {

    private final MeterService meterService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
    @Operation(summary = "Register a new meter for a customer")
    public ApiResponse<MeterResponse> create(@Valid @RequestBody MeterRequest request) {
        return ApiResponse.ok("Meter created", meterService.create(request));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR', 'FINANCE')")
    @Operation(summary = "Get meter by ID")
    public ApiResponse<MeterResponse> findById(@PathVariable Long id) {
        return ApiResponse.ok(meterService.findById(id));
    }

    @GetMapping("/customer/{customerId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR', 'FINANCE', 'CUSTOMER')")
    @Operation(summary = "List meters for a customer")
    public ApiResponse<List<MeterResponse>> findByCustomer(@PathVariable Long customerId) {
        return ApiResponse.ok(meterService.findByCustomer(customerId));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Activate or deactivate a meter")
    public ApiResponse<MeterResponse> updateStatus(@PathVariable Long id, @RequestParam MeterStatus status) {
        return ApiResponse.ok("Meter status updated", meterService.updateStatus(id, status));
    }
}
