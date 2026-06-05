package com.wasac.billing.service;

import com.wasac.billing.domain.entity.Customer;
import com.wasac.billing.domain.entity.Meter;
import com.wasac.billing.domain.enums.MeterStatus;
import com.wasac.billing.dto.request.MeterRequest;
import com.wasac.billing.dto.response.MeterResponse;
import com.wasac.billing.exception.DuplicateResourceException;
import com.wasac.billing.exception.ResourceNotFoundException;
import com.wasac.billing.repository.MeterRepository;
import com.wasac.billing.security.SecurityHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MeterService {

    private final MeterRepository meterRepository;
    private final CustomerService customerService;
    private final SecurityHelper securityHelper;

    @Transactional
    public MeterResponse create(MeterRequest request) {
        if (meterRepository.existsByMeterNumber(request.getMeterNumber())) {
            throw new DuplicateResourceException("Meter number already exists: " + request.getMeterNumber());
        }

        Customer customer = customerService.getEntityById(request.getCustomerId());

        Meter meter = Meter.builder()
                .meterNumber(request.getMeterNumber())
                .meterType(request.getMeterType())
                .installationDate(request.getInstallationDate())
                .status(request.getStatus() != null ? request.getStatus() : MeterStatus.ACTIVE)
                .customer(customer)
                .build();

        return MeterResponse.from(meterRepository.save(meter));
    }

    @Transactional(readOnly = true)
    public MeterResponse findById(Long id) {
        Meter meter = getEntityById(id);
        securityHelper.assertCustomerOwnsData(meter.getCustomer().getId());
        return MeterResponse.from(meter);
    }

    @Transactional(readOnly = true)
    public Meter getEntityById(Long id) {
        return meterRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Meter not found: " + id));
    }

    @Transactional(readOnly = true)
    public List<MeterResponse> findByCustomer(Long customerId) {
        securityHelper.assertCustomerOwnsData(customerId);
        return meterRepository.findByCustomerId(customerId).stream()
                .map(MeterResponse::from)
                .toList();
    }

    @Transactional
    public MeterResponse updateStatus(Long id, MeterStatus status) {
        Meter meter = getEntityById(id);
        meter.setStatus(status);
        return MeterResponse.from(meterRepository.save(meter));
    }
}
