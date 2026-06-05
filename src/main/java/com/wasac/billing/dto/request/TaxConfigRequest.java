package com.wasac.billing.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class TaxConfigRequest {

    @NotBlank
    private String name;

    @NotNull
    @DecimalMin("0.0")
    @DecimalMax("100.0")
    private BigDecimal rate;

    @NotNull(message = "Effective from date is required")
    private LocalDate effectiveFrom;
}
