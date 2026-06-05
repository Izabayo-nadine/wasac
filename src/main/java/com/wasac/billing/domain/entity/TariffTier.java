package com.wasac.billing.domain.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

/** Tier band for tier-based tariff calculation */
@Entity
@Table(name = "tariff_tiers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TariffTier {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tariff_id", nullable = false)
    private Tariff tariff;

    @Column(name = "tier_order", nullable = false)
    private Integer tierOrder;

    @Column(name = "min_units", nullable = false, precision = 12, scale = 2)
    private BigDecimal minUnits;

    @Column(name = "max_units", precision = 12, scale = 2)
    private BigDecimal maxUnits;

    @Column(name = "rate_per_unit", nullable = false, precision = 12, scale = 4)
    private BigDecimal ratePerUnit;
}
