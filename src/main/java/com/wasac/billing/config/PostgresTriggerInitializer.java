package com.wasac.billing.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.Statement;

/**
 * Installs PostgreSQL triggers for bill and payment notifications after schema is created.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class PostgresTriggerInitializer {

    private final DataSource dataSource;

    @Value("${app.db.triggers.enabled:true}")
    private boolean triggersEnabled;

    @Value("${spring.datasource.url}")
    private String datasourceUrl;

    @EventListener(ApplicationReadyEvent.class)
    public void installTriggers() {
        if (!triggersEnabled || !datasourceUrl.contains("postgresql")) {
            log.info("PostgreSQL trigger installation skipped");
            return;
        }

        try {
            ClassPathResource resource = new ClassPathResource("db/triggers-postgres.sql");
            String sql = resource.getContentAsString(StandardCharsets.UTF_8);

            try (Connection connection = dataSource.getConnection();
                 Statement statement = connection.createStatement()) {
                statement.execute(sql);
            }

            log.info("PostgreSQL triggers installed successfully");
        } catch (Exception ex) {
            log.error("Failed to install PostgreSQL triggers: {}", ex.getMessage());
        }
    }
}
