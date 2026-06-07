package com.wasac.billing.config;

import com.wasac.billing.domain.entity.*;
import com.wasac.billing.domain.enums.*;
import com.wasac.billing.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

/**
 * Seeds roles, default users, tariffs, and sample data for demo/testing.
 */
@Component
@Order(2)
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;
    private final MeterRepository meterRepository;
    private final TariffRepository tariffRepository;
    private final TaxConfigRepository taxConfigRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        ensureAllRoles();

        if (userRepository.existsByEmail("customer@example.rw")) {
            log.info("Demo data already present, skipping sample data seeding");
            return;
        }

        log.info("Seeding database with initial data...");

        Role adminRole = roleRepository.findByName(RoleName.ROLE_ADMIN).orElseThrow();
        Role operatorRole = roleRepository.findByName(RoleName.ROLE_OPERATOR).orElseThrow();
        Role financeRole = roleRepository.findByName(RoleName.ROLE_FINANCE).orElseThrow();
        Role customerRole = roleRepository.findByName(RoleName.ROLE_CUSTOMER).orElseThrow();

        String adminPassword = passwordEncoder.encode("admin123");
        String demoPassword = passwordEncoder.encode("Password@123");

        if (!userRepository.existsByEmail("admin@wasac.com")) {
            userRepository.save(User.builder()
                    .fullNames("System Administrator")
                    .email("admin@wasac.com")
                    .phoneNumber("0788000001")
                    .password(adminPassword)
                    .status(UserStatus.ACTIVE)
                    .emailVerified(true)
                    .roles(Set.of(adminRole))
                    .build());
        }

        userRepository.save(User.builder()
                .fullNames("Meter Operator")
                .email("operator@wasac.rw")
                .phoneNumber("0788000002")
                .password(demoPassword)
                .status(UserStatus.ACTIVE)
                .emailVerified(true)
                .roles(Set.of(operatorRole))
                .build());

        userRepository.save(User.builder()
                .fullNames("Finance Officer")
                .email("finance@wasac.rw")
                .phoneNumber("0788000003")
                .password(demoPassword)
                .status(UserStatus.ACTIVE)
                .emailVerified(true)
                .roles(Set.of(financeRole))
                .build());

        User customerUser = userRepository.save(User.builder()
                .fullNames("Jean Baptiste Uwimana")
                .email("customer@example.rw")
                .phoneNumber("0788000004")
                .password(demoPassword)
                .status(UserStatus.ACTIVE)
                .emailVerified(true)
                .roles(Set.of(customerRole))
                .build());

        // Sample customer
        Customer customer = customerRepository.save(Customer.builder()
                .fullNames("Jean Baptiste Uwimana")
                .nationalId("1199880012345678")
                .email("customer@example.rw")
                .phoneNumber("0788000004")
                .address("KG 123 St, Kigali, Rwanda")
                .status(CustomerStatus.ACTIVE)
                .user(customerUser)
                .build());
        customerUser.setCustomer(customer);

        // Meters
        meterRepository.save(Meter.builder()
                .meterNumber("WTR-001-2024")
                .meterType(MeterType.WATER)
                .installationDate(LocalDate.of(2024, 1, 15))
                .status(MeterStatus.ACTIVE)
                .customer(customer)
                .build());

        meterRepository.save(Meter.builder()
                .meterNumber("ELC-001-2024")
                .meterType(MeterType.ELECTRICITY)
                .installationDate(LocalDate.of(2024, 1, 15))
                .status(MeterStatus.ACTIVE)
                .customer(customer)
                .build());

        // Water tariff (flat)
        tariffRepository.save(Tariff.builder()
                .name("Residential Water Tariff")
                .meterType(MeterType.WATER)
                .tariffType(TariffType.FLAT)
                .flatRate(new BigDecimal("350.00"))
                .serviceCharge(new BigDecimal("1500.00"))
                .version(1)
                .effectiveFrom(LocalDate.of(2024, 1, 1))
                .active(true)
                .build());

        // Electricity tariff (tier-based)
        Tariff elecTariff = Tariff.builder()
                .name("Residential Electricity Tariff")
                .meterType(MeterType.ELECTRICITY)
                .tariffType(TariffType.TIER_BASED)
                .serviceCharge(new BigDecimal("2000.00"))
                .version(1)
                .effectiveFrom(LocalDate.of(2024, 1, 1))
                .active(true)
                .build();

        elecTariff.getTiers().add(TariffTier.builder()
                .tariff(elecTariff).tierOrder(1)
                .minUnits(BigDecimal.ZERO).maxUnits(new BigDecimal("50"))
                .ratePerUnit(new BigDecimal("120.00")).build());
        elecTariff.getTiers().add(TariffTier.builder()
                .tariff(elecTariff).tierOrder(2)
                .minUnits(new BigDecimal("50")).maxUnits(new BigDecimal("150"))
                .ratePerUnit(new BigDecimal("180.00")).build());
        elecTariff.getTiers().add(TariffTier.builder()
                .tariff(elecTariff).tierOrder(3)
                .minUnits(new BigDecimal("150")).maxUnits(null)
                .ratePerUnit(new BigDecimal("250.00")).build());

        tariffRepository.save(elecTariff);

        // VAT 18%
        taxConfigRepository.save(TaxConfig.builder()
                .name("VAT")
                .rate(new BigDecimal("18.00"))
                .version(1)
                .effectiveFrom(LocalDate.of(2024, 1, 1))
                .active(true)
                .build());

        log.info("Database seeded successfully!");
        log.info("Default admin credentials - admin@wasac.com / admin123");
        log.info("Swagger UI: http://localhost:8080/swagger-ui.html");
    }

    private void ensureAllRoles() {
        for (RoleName roleName : RoleName.values()) {
            roleRepository.findByName(roleName)
                    .orElseGet(() -> roleRepository.save(Role.builder().name(roleName).build()));
        }
    }
}
