package com.cryptovault.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;

/**
 * Конфигурация базы данных для разных профилей.
 * Spring Boot автоматически настроит DataSource и Flyway на основе application.yml.
 * Этот класс используется для валидации подключения и логирования информации о БД.
 */
@Configuration
public class DatabaseConfig {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseConfig.class);

    /**
     * Валидация подключения к H2 в dev профиле
     */
    @Configuration
    @Profile("dev")
    static class DevDatabaseConfig {
        
        @Autowired
        private DataSource dataSource;

        @org.springframework.context.annotation.Bean
        public String validateDevConnection() {
            try (Connection connection = dataSource.getConnection()) {
                DatabaseMetaData metaData = connection.getMetaData();
                logger.info("========================================");
                logger.info("Database connection validated successfully");
                logger.info("Database: {}", metaData.getDatabaseProductName());
                logger.info("Version: {}", metaData.getDatabaseProductVersion());
                logger.info("Driver: {}", metaData.getDriverName());
                logger.info("URL: {}", metaData.getURL());
                logger.info("========================================");
            } catch (Exception e) {
                logger.error("Failed to validate database connection", e);
                throw new RuntimeException("Database connection validation failed", e);
            }
            return "dev-validated";
        }
    }

    /**
     * Валидация подключения к PostgreSQL в prod профиле
     */
    @Configuration
    @Profile("prod")
    static class ProdDatabaseConfig {
        
        @Autowired
        private DataSource dataSource;

        @org.springframework.context.annotation.Bean
        public String validateProdConnection() {
            try (Connection connection = dataSource.getConnection()) {
                DatabaseMetaData metaData = connection.getMetaData();
                logger.info("========================================");
                logger.info("Database connection validated successfully");
                logger.info("Database: {}", metaData.getDatabaseProductName());
                logger.info("Version: {}", metaData.getDatabaseProductVersion());
                logger.info("Driver: {}", metaData.getDriverName());
                logger.info("URL: {}", metaData.getURL());
                logger.info("========================================");
            } catch (Exception e) {
                logger.error("Failed to validate database connection", e);
                throw new RuntimeException("Database connection validation failed", e);
            }
            return "prod-validated";
        }
    }
}
