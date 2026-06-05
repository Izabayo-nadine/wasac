package com.wasac.billing.controller;

import com.wasac.billing.dto.request.MeterReadingRequest;
import com.wasac.billing.dto.response.ApiResponse;
import com.wasac.billing.dto.response.MeterReadingResponse;
import com.wasac.billing.service.MeterReadingService;
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
@RequestMapping("/api/meter-readings")
@RequiredArgsConstructor
@Tag(name = "Meter Readings", description = "Capture and view meter readings")
@SecurityRequirement(name = "bearerAuth")
public class MeterReadingController {

    private final MeterReadingService readingService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('OPERATOR')")
    @Operation(summary = "Capture a meter reading (Operator only)")
    public ApiResponse<MeterReadingResponse> capture(@Valid @RequestBody MeterReadingRequest request) {
        return ApiResponse.ok("Reading captured", readingService.capture(request));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR', 'FINANCE')")
    @Operation(summary = "List all meter readings")
    public ApiResponse<List<MeterReadingResponse>> findAll() {
        return ApiResponse.ok(readingService.findAll());
    }

    @GetMapping("/meter/{meterId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR', 'FINANCE')")
    @Operation(summary = "List readings for a meter")
    public ApiResponse<List<MeterReadingResponse>> findByMeter(@PathVariable Long meterId) {
        return ApiResponse.ok(readingService.findByMeter(meterId));
    }
}
