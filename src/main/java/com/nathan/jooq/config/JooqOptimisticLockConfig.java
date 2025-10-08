package com.nathan.jooq.config;

import org.springframework.boot.autoconfigure.jooq.DefaultConfigurationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JooqOptimisticLockConfig {
    @Bean
    public DefaultConfigurationCustomizer jooqConfigCustomizer() {
        return c -> c.settings()
                .withExecuteWithOptimisticLocking(true)
                .withUpdateRecordTimestamp(true);
    }
}
