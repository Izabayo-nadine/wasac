package com.wasac.billing.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Applies missing schema changes when Hibernate ddl-auto=update does not alter existing tables.
 */
@Component
@Order(0)
@RequiredArgsConstructor
@Slf4j
public class SchemaMigrationRunner implements CommandLineRunner {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) {
        try {
            jdbcTemplate.execute(
                    "ALTER TABLE users ADD COLUMN IF NOT EXISTS email_verified BOOLEAN NOT NULL DEFAULT true");

            jdbcTemplate.execute("""
                    CREATE TABLE IF NOT EXISTS email_otps (
                        id          BIGSERIAL PRIMARY KEY,
                        email       VARCHAR(255) NOT NULL,
                        otp_code    VARCHAR(10)  NOT NULL,
                        expires_at  TIMESTAMP    NOT NULL,
                        used        BOOLEAN      NOT NULL DEFAULT false,
                        created_at  TIMESTAMP    DEFAULT NOW()
                    )""");

            jdbcTemplate.execute("""
                    CREATE TABLE IF NOT EXISTS revoked_tokens (
                        id          BIGSERIAL PRIMARY KEY,
                        token_hash  VARCHAR(64) NOT NULL UNIQUE,
                        expires_at  TIMESTAMP   NOT NULL,
                        revoked_at  TIMESTAMP   DEFAULT NOW()
                    )""");

            log.info("Database schema migration completed successfully");
        } catch (Exception ex) {
            log.error("Database schema migration failed: {}", ex.getMessage());
            throw new IllegalStateException("Failed to apply database schema updates", ex);
        }
    }
}
