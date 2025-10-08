package com.nathan.jooq.config;

import jakarta.persistence.EntityManagerFactory;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.jooq.impl.DefaultDSLContext;
import org.springframework.boot.autoconfigure.jooq.SpringTransactionProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.jpa.JpaTransactionManager;

import javax.sql.DataSource;

@Configuration
public class JooqConfig {

    @Bean
    public DefaultDSLContext dslContext(org.jooq.Configuration jooqConfig) {
        return new DefaultDSLContext(jooqConfig);
    }

    @Bean
    public SpringTransactionProvider transactionProvider(EntityManagerFactory emf) {
        return new SpringTransactionProvider(
                new JpaTransactionManager(emf)
        );
    }

}
