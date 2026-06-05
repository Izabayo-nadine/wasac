package com.wasac.billing.controller;

import com.wasac.billing.dto.request.BillGenerationRequest;
import com.wasac.billing.dto.response.ApiResponse;
import com.wasac.billing.dto.response.BillResponse;
import com.wasac.billing.service.BillService;
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
@RequestMapping("/api/bills")
@RequiredArgsConstructor
@Tag(name = "Bills", description = "Bill generation and approval")
@SecurityRequirement(name = "bearerAuth")
public class BillController {

    private final BillService billService;

    @PostMapping("/generate")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE')")
    @Operation(summary = "Generate monthly bills from meter readings")
    public ApiResponse<List<BillResponse>> generate(@Valid @RequestBody BillGenerationRequest request) {
        return ApiResponse.ok("Bills generated", billService.generateBills(request));
    }

    @PatchMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE')")
    @Operation(summary = "Approve a pending bill")
    public ApiResponse<BillResponse> approve(@PathVariable Long id) {
        return ApiResponse.ok("Bill approved", billService.approveBill(id));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE')")
    @Operation(summary = "List all bills")
    public ApiResponse<List<BillResponse>> findAll() {
        return ApiResponse.ok(billService.findAll());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE', 'CUSTOMER')")
    @Operation(summary = "Get bill by ID")
    public ApiResponse<BillResponse> findById(@PathVariable Long id) {
        return ApiResponse.ok(billService.findById(id));
    }

    @GetMapping("/customer/{customerId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE', 'CUSTOMER')")
    @Operation(summary = "List bills for a customer")
    public ApiResponse<List<BillResponse>> findByCustomer(@PathVariable Long customerId) {
        return ApiResponse.ok(billService.findByCustomer(customerId));
    }
}
