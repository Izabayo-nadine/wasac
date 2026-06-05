package com.wasac.billing.service;

import com.wasac.billing.domain.entity.PenaltyConfig;
import com.wasac.billing.domain.entity.TaxConfig;
import com.wasac.billing.dto.request.PenaltyConfigRequest;
import com.wasac.billing.dto.request.TaxConfigRequest;
import com.wasac.billing.repository.PenaltyConfigRepository;
import com.wasac.billing.repository.TaxConfigRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TaxPenaltyService {

    private final TaxConfigRepository taxConfigRepository;
    private final PenaltyConfigRepository penaltyConfigRepository;

    @Transactional
    public TaxConfig createTax(TaxConfigRequest request) {
        int version = taxConfigRepository.findAll().stream()
                .mapToInt(TaxConfig::getVersion)
                .max().orElse(0) + 1;

        taxConfigRepository.findAll().stream()
                .filter(TaxConfig::getActive)
                .forEach(t -> {
                    t.setEffectiveTo(request.getEffectiveFrom().minusDays(1));
                    t.setActive(false);
                    taxConfigRepository.save(t);
                });

        TaxConfig tax = TaxConfig.builder()
                .name(request.getName())
                .rate(request.getRate())
                .version(version)
                .effectiveFrom(request.getEffectiveFrom())
                .active(true)
                .build();

        return taxConfigRepository.save(tax);
    }

    @Transactional
    public PenaltyConfig createPenalty(PenaltyConfigRequest request) {
        int version = penaltyConfigRepository.findAll().stream()
                .mapToInt(PenaltyConfig::getVersion)
                .max().orElse(0) + 1;

        penaltyConfigRepository.findAll().stream()
                .filter(PenaltyConfig::getActive)
                .forEach(p -> {
                    p.setEffectiveTo(request.getEffectiveFrom().minusDays(1));
                    p.setActive(false);
                    penaltyConfigRepository.save(p);
                });

        PenaltyConfig penalty = PenaltyConfig.builder()
                .name(request.getName())
                .rate(request.getRate())
                .graceDays(request.getGraceDays() != null ? request.getGraceDays() : 0)
                .version(version)
                .effectiveFrom(request.getEffectiveFrom())
                .active(true)
                .build();

        return penaltyConfigRepository.save(penalty);
    }

    @Transactional(readOnly = true)
    public List<TaxConfig> findAllTaxes() {
        return taxConfigRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<PenaltyConfig> findAllPenalties() {
        return penaltyConfigRepository.findAll();
    }

    @Transactional(readOnly = true)
    public TaxConfig findEffectiveTax(LocalDate date) {
        return taxConfigRepository.findEffectiveTax(date).orElse(null);
    }

    @Transactional(readOnly = true)
    public PenaltyConfig findEffectivePenalty(LocalDate date) {
        return penaltyConfigRepository.findEffectivePenalty(date).orElse(null);
    }
}
