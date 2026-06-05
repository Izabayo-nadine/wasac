package com.wasac.billing.service;

import com.wasac.billing.domain.entity.Tariff;
import com.wasac.billing.domain.entity.TariffTier;
import com.wasac.billing.domain.enums.MeterType;
import com.wasac.billing.domain.enums.TariffType;
import com.wasac.billing.dto.request.TariffRequest;
import com.wasac.billing.dto.request.TariffTierRequest;
import com.wasac.billing.exception.BusinessRuleException;
import com.wasac.billing.exception.ResourceNotFoundException;
import com.wasac.billing.repository.TariffRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TariffService {

    private final TariffRepository tariffRepository;

    /**
     * Creates a new tariff version. Previous active tariff for same meter type is closed.
     * New tariffs apply only to future billing cycles.
     */
    @Transactional
    public Tariff create(TariffRequest request) {
        validateTariffRequest(request);

        int nextVersion = tariffRepository.findTopByMeterTypeOrderByVersionDesc(request.getMeterType())
                .map(t -> t.getVersion() + 1)
                .orElse(1);

        // Close previous active tariff version
        tariffRepository.findByMeterTypeAndActiveTrue(request.getMeterType()).forEach(t -> {
            if (t.getEffectiveTo() == null || t.getEffectiveTo().isAfter(request.getEffectiveFrom())) {
                t.setEffectiveTo(request.getEffectiveFrom().minusDays(1));
                t.setActive(false);
                tariffRepository.save(t);
            }
        });

        Tariff tariff = Tariff.builder()
                .name(request.getName())
                .meterType(request.getMeterType())
                .tariffType(request.getTariffType())
                .flatRate(request.getFlatRate())
                .serviceCharge(request.getServiceCharge())
                .version(nextVersion)
                .effectiveFrom(request.getEffectiveFrom())
                .active(true)
                .build();

        if (request.getTariffType() == TariffType.TIER_BASED && request.getTiers() != null) {
            for (TariffTierRequest tierReq : request.getTiers()) {
                TariffTier tier = TariffTier.builder()
                        .tariff(tariff)
                        .tierOrder(tierReq.getTierOrder())
                        .minUnits(tierReq.getMinUnits())
                        .maxUnits(tierReq.getMaxUnits())
                        .ratePerUnit(tierReq.getRatePerUnit())
                        .build();
                tariff.getTiers().add(tier);
            }
        }

        return tariffRepository.save(tariff);
    }

    @Transactional(readOnly = true)
    public List<Tariff> findAll() {
        return tariffRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Tariff findEffective(MeterType meterType, LocalDate billingDate) {
        return tariffRepository.findEffectiveTariff(meterType, billingDate)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No effective tariff found for " + meterType + " on " + billingDate));
    }

    /** Calculate consumption charge based on tariff type */
    public BigDecimal calculateConsumptionCharge(Tariff tariff, BigDecimal consumption) {
        if (tariff.getTariffType() == TariffType.FLAT) {
            return consumption.multiply(tariff.getFlatRate()).setScale(2, RoundingMode.HALF_UP);
        }

        // Tier-based calculation
        BigDecimal remaining = consumption;
        BigDecimal total = BigDecimal.ZERO;
        List<TariffTier> tiers = tariff.getTiers().stream()
                .sorted(Comparator.comparing(TariffTier::getTierOrder))
                .toList();

        for (TariffTier tier : tiers) {
            if (remaining.compareTo(BigDecimal.ZERO) <= 0) break;

            BigDecimal tierMax = tier.getMaxUnits() != null
                    ? tier.getMaxUnits().subtract(tier.getMinUnits())
                    : remaining;
            BigDecimal unitsInTier = remaining.min(tierMax);
            total = total.add(unitsInTier.multiply(tier.getRatePerUnit()));
            remaining = remaining.subtract(unitsInTier);
        }

        return total.setScale(2, RoundingMode.HALF_UP);
    }

    private void validateTariffRequest(TariffRequest request) {
        if (request.getTariffType() == TariffType.FLAT && request.getFlatRate() == null) {
            throw new BusinessRuleException("Flat rate is required for FLAT tariff type");
        }
        if (request.getTariffType() == TariffType.TIER_BASED
                && (request.getTiers() == null || request.getTiers().isEmpty())) {
            throw new BusinessRuleException("At least one tier is required for TIER_BASED tariff");
        }
    }
}
