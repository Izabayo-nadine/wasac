package com.wasac.billing.service;

import com.wasac.billing.domain.entity.Meter;
import com.wasac.billing.domain.entity.MeterReading;
import com.wasac.billing.domain.entity.User;
import com.wasac.billing.domain.enums.MeterStatus;
import com.wasac.billing.dto.request.MeterReadingRequest;
import com.wasac.billing.dto.response.MeterReadingResponse;
import com.wasac.billing.exception.BusinessRuleException;
import com.wasac.billing.exception.DuplicateResourceException;
import com.wasac.billing.exception.ResourceNotFoundException;
import com.wasac.billing.repository.MeterReadingRepository;
import com.wasac.billing.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MeterReadingService {

    private final MeterReadingRepository readingRepository;
    private final MeterService meterService;
    private final UserRepository userRepository;

    @Transactional
    public MeterReadingResponse capture(MeterReadingRequest request) {
        Meter meter = meterService.getEntityById(request.getMeterId());

        // Rule: meter must be active
        if (meter.getStatus() != MeterStatus.ACTIVE) {
            throw new BusinessRuleException("Cannot capture reading for inactive meter");
        }

        // Rule: current reading must be greater than previous
        if (request.getCurrentReading().compareTo(request.getPreviousReading()) <= 0) {
            throw new BusinessRuleException("Current reading must be greater than previous reading");
        }

        int month = request.getReadingDate().getMonthValue();
        int year = request.getReadingDate().getYear();

        // Rule: only one reading per meter per month/year
        if (readingRepository.existsByMeterIdAndBillingMonthAndBillingYear(meter.getId(), month, year)) {
            throw new DuplicateResourceException(
                    "A reading already exists for meter " + meter.getMeterNumber() + " in " + month + "/" + year);
        }

        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User operator = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Operator not found"));

        MeterReading reading = MeterReading.builder()
                .meter(meter)
                .previousReading(request.getPreviousReading())
                .currentReading(request.getCurrentReading())
                .readingDate(request.getReadingDate())
                .billingMonth(month)
                .billingYear(year)
                .capturedBy(operator)
                .build();

        return MeterReadingResponse.from(readingRepository.save(reading));
    }

    @Transactional(readOnly = true)
    public List<MeterReadingResponse> findByMeter(Long meterId) {
        return readingRepository.findByMeterId(meterId).stream()
                .map(MeterReadingResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<MeterReadingResponse> findAll() {
        return readingRepository.findAll().stream()
                .map(MeterReadingResponse::from)
                .toList();
    }
}
