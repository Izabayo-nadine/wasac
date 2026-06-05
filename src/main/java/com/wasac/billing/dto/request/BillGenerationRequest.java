package com.wasac.billing.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class BillGenerationRequest {

    @NotNull
    @Min(1) @Max(12)
    private Integer billingMonth;

    @NotNull
    @Min(2000)
    private Integer billingYear;

    private Long customerId;
}
