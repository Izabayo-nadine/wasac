package com.wasac.billing.config;

import com.wasac.billing.domain.entity.Role;
import com.wasac.billing.domain.entity.User;
import com.wasac.billing.domain.enums.RoleName;
import com.wasac.billing.domain.enums.UserStatus;
import com.wasac.billing.repository.RoleRepository;
import com.wasac.billing.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

/**
 * Ensures default admin exists even when the database was seeded before admin@wasac.com was introduced.
 */
@Component
@Order(1)
@RequiredArgsConstructor
@Slf4j
public class DefaultAdminEnsurer implements CommandLineRunner {

    private static final String ADMIN_EMAIL = "admin@wasac.com";
    private static final String LEGACY_ADMIN_EMAIL = "admin@wasac.rw";
    private static final String ADMIN_PASSWORD = "admin123";

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        Role adminRole = roleRepository.findByName(RoleName.ROLE_ADMIN)
                .orElseGet(() -> roleRepository.save(Role.builder().name(RoleName.ROLE_ADMIN).build()));

        userRepository.findByEmail(ADMIN_EMAIL).ifPresentOrElse(
                this::ensureAdminActive,
                () -> migrateOrCreateAdmin(adminRole)
        );
    }

    private void ensureAdminActive(User admin) {
        admin.setPassword(passwordEncoder.encode(ADMIN_PASSWORD));
        admin.setEmailVerified(true);
        admin.setStatus(UserStatus.ACTIVE);
        userRepository.save(admin);
        log.info("Default admin ready: {} / {}", ADMIN_EMAIL, ADMIN_PASSWORD);
    }

    private void migrateOrCreateAdmin(Role adminRole) {
        userRepository.findByEmail(LEGACY_ADMIN_EMAIL).ifPresentOrElse(
                legacy -> {
                    legacy.setEmail(ADMIN_EMAIL);
                    legacy.setPassword(passwordEncoder.encode(ADMIN_PASSWORD));
                    legacy.setEmailVerified(true);
                    legacy.setStatus(UserStatus.ACTIVE);
                    legacy.setRoles(new HashSet<>(Set.of(adminRole)));
                    userRepository.save(legacy);
                    log.info("Migrated legacy admin {} -> {}", LEGACY_ADMIN_EMAIL, ADMIN_EMAIL);
                },
                () -> {
                    userRepository.save(User.builder()
                            .fullNames("System Administrator")
                            .email(ADMIN_EMAIL)
                            .phoneNumber("0788000001")
                            .password(passwordEncoder.encode(ADMIN_PASSWORD))
                            .status(UserStatus.ACTIVE)
                            .emailVerified(true)
                            .roles(new HashSet<>(Set.of(adminRole)))
                            .build());
                    log.info("Created default admin: {} / {}", ADMIN_EMAIL, ADMIN_PASSWORD);
                }
        );
    }
}
