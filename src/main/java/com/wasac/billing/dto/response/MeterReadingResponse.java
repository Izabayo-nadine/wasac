package com.wasac.billing.dto.response;

import com.wasac.billing.domain.entity.MeterReading;
import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Value
@Builder
public class MeterReadingResponse {

    Long id;
    Long meterId;
    String meterNumber;
    BigDecimal previousReading;
    BigDecimal currentReading;
    BigDecimal consumption;
    LocalDate readingDate;
    Integer billingMonth;
    Integer billingYear;
    Long capturedByUserId;
    String capturedByName;
    LocalDateTime createdAt;

    public static MeterReadingResponse from(MeterReading reading) {
        if (reading == null) {
            return null;
        }
        Long meterId = null;
        String meterNumber = null;
        if (reading.getMeter() != null) {
            meterId = reading.getMeter().getId();
            meterNumber = reading.getMeter().getMeterNumber();
        }
        Long capturedByUserId = null;
        String capturedByName = null;
        if (reading.getCapturedBy() != null) {
            capturedByUserId = reading.getCapturedBy().getId();
            capturedByName = reading.getCapturedBy().getFullNames();
        }
        return MeterReadingResponse.builder()
                .id(reading.getId())
                .meterId(meterId)
                .meterNumber(meterNumber)
                .previousReading(reading.getPreviousReading())
                .currentReading(reading.getCurrentReading())
                .consumption(reading.getConsumption())
                .readingDate(reading.getReadingDate())
                .billingMonth(reading.getBillingMonth())
                .billingYear(reading.getBillingYear())
                .capturedByUserId(capturedByUserId)
                .capturedByName(capturedByName)
                .createdAt(reading.getCreatedAt())
                .build();
    }
}
