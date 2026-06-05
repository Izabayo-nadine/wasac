package com.wasac.billing.dto.response;

import com.wasac.billing.domain.entity.BillLineItem;
import com.wasac.billing.domain.enums.MeterType;
import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;

@Value
@Builder
public class BillLineItemResponse {

    Long id;
    Long meterId;
    String meterNumber;
    MeterType meterType;
    BigDecimal consumption;
    BigDecimal amount;
    String description;

    public static BillLineItemResponse from(BillLineItem item) {
        if (item == null) {
            return null;
        }
        Long meterId = null;
        String meterNumber = null;
        if (item.getMeter() != null) {
            meterId = item.getMeter().getId();
            meterNumber = item.getMeter().getMeterNumber();
        }
        return BillLineItemResponse.builder()
                .id(item.getId())
                .meterId(meterId)
                .meterNumber(meterNumber)
                .meterType(item.getMeterType())
                .consumption(item.getConsumption())
                .amount(item.getAmount())
                .description(item.getDescription())
                .build();
    }
}
