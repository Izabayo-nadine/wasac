package com.wasac.billing.controller;

import com.wasac.billing.domain.entity.PenaltyConfig;
import com.wasac.billing.domain.entity.TaxConfig;
import com.wasac.billing.dto.request.PenaltyConfigRequest;
import com.wasac.billing.dto.request.TaxConfigRequest;
import com.wasac.billing.dto.response.ApiResponse;
import com.wasac.billing.service.TaxPenaltyService;
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
@RequestMapping("/api/config")
@RequiredArgsConstructor
@Tag(name = "Tax & Penalty", description = "Tax and penalty configuration (Admin)")
@SecurityRequirement(name = "bearerAuth")
public class TaxPenaltyController {

    private final TaxPenaltyService taxPenaltyService;

    @PostMapping("/tax")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create a new tax configuration version")
    public ApiResponse<TaxConfig> createTax(@Valid @RequestBody TaxConfigRequest request) {
        return ApiResponse.ok("Tax config created", taxPenaltyService.createTax(request));
    }

    @PostMapping("/penalty")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create a new penalty configuration version")
    public ApiResponse<PenaltyConfig> createPenalty(@Valid @RequestBody PenaltyConfigRequest request) {
        return ApiResponse.ok("Penalty config created", taxPenaltyService.createPenalty(request));
    }

    @GetMapping("/tax")
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE')")
    @Operation(summary = "List all tax configurations")
    public ApiResponse<List<TaxConfig>> findAllTaxes() {
        return ApiResponse.ok(taxPenaltyService.findAllTaxes());
    }

    @GetMapping("/penalty")
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE')")
    @Operation(summary = "List all penalty configurations")
    public ApiResponse<List<PenaltyConfig>> findAllPenalties() {
        return ApiResponse.ok(taxPenaltyService.findAllPenalties());
    }
}
