package com.wasac.billing.dto.response;

import com.wasac.billing.domain.entity.Meter;
import com.wasac.billing.domain.enums.MeterStatus;
import com.wasac.billing.domain.enums.MeterType;
import lombok.Builder;
import lombok.Value;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Value
@Builder
public class MeterResponse {

    Long id;
    String meterNumber;
    MeterType meterType;
    LocalDate installationDate;
    MeterStatus status;
    Long customerId;
    String customerName;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;

    public static MeterResponse from(Meter meter) {
        if (meter == null) {
            return null;
        }
        Long customerId = null;
        String customerName = null;
        if (meter.getCustomer() != null) {
            customerId = meter.getCustomer().getId();
            customerName = meter.getCustomer().getFullNames();
        }
        return MeterResponse.builder()
                .id(meter.getId())
                .meterNumber(meter.getMeterNumber())
                .meterType(meter.getMeterType())
                .installationDate(meter.getInstallationDate())
                .status(meter.getStatus())
                .customerId(customerId)
                .customerName(customerName)
                .createdAt(meter.getCreatedAt())
                .updatedAt(meter.getUpdatedAt())
                .build();
    }
}
