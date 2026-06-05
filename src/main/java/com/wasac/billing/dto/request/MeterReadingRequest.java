package com.wasac.billing.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class MeterReadingRequest {

    @NotNull
    private Long meterId;

    @NotNull
    @DecimalMin(value = "0.0", inclusive = true)
    private BigDecimal previousReading;

    @NotNull
    @DecimalMin(value = "0.0", inclusive = true)
    private BigDecimal currentReading;

    @NotNull(message = "Reading date is required")
    @PastOrPresent(message = "Reading date cannot be in the future")
    private LocalDate readingDate;
}
