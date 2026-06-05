package com.wasac.billing.controller;

import com.wasac.billing.domain.entity.Tariff;
import com.wasac.billing.domain.enums.MeterType;
import com.wasac.billing.dto.request.TariffRequest;
import com.wasac.billing.dto.response.ApiResponse;
import com.wasac.billing.service.TariffService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/tariffs")
@RequiredArgsConstructor
@Tag(name = "Tariffs", description = "Versioned tariff configuration (Admin)")
@SecurityRequirement(name = "bearerAuth")
public class TariffController {

    private final TariffService tariffService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create a new tariff version")
    public ApiResponse<Tariff> create(@Valid @RequestBody TariffRequest request) {
        return ApiResponse.ok("Tariff created", tariffService.create(request));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE')")
    @Operation(summary = "List all tariffs")
    public ApiResponse<List<Tariff>> findAll() {
        return ApiResponse.ok(tariffService.findAll());
    }

    @GetMapping("/effective")
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE')")
    @Operation(summary = "Get effective tariff for meter type and date")
    public ApiResponse<Tariff> findEffective(
            @RequestParam MeterType meterType,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ApiResponse.ok(tariffService.findEffective(meterType, date));
    }
}
