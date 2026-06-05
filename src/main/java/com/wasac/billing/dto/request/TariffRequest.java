package com.wasac.billing.dto.request;

import com.wasac.billing.domain.enums.MeterType;
import com.wasac.billing.domain.enums.TariffType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
public class TariffRequest {

    @NotBlank
    private String name;

    @NotNull
    private MeterType meterType;

    @NotNull
    private TariffType tariffType;

    @DecimalMin("0.0")
    private BigDecimal flatRate;

    @NotNull
    @DecimalMin("0.0")
    private BigDecimal serviceCharge;

    @NotNull(message = "Effective from date is required")
    private LocalDate effectiveFrom;

    @Valid
    private List<TariffTierRequest> tiers;
}
