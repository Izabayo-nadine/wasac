package com.wasac.billing.dto.request;

import com.wasac.billing.domain.enums.MeterStatus;
import com.wasac.billing.domain.enums.MeterType;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;

@Data
public class MeterRequest {

    @NotBlank
    @Size(max = 50)
    private String meterNumber;

    @NotNull
    private MeterType meterType;

    @NotNull(message = "Installation date is required")
    @PastOrPresent(message = "Installation date cannot be in the future")
    private LocalDate installationDate;

    private MeterStatus status;

    @NotNull
    private Long customerId;
}
