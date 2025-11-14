package com.cryptovault.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Configuration
public class ProfilesConfig {

    private static final Logger logger = LoggerFactory.getLogger(ProfilesConfig.class);

    @Configuration
    @Profile("dev")
    static class DevProfile {
        @PostConstruct
        public void init() {
            logger.info("========================================");
            logger.info("CryptoVault is operating in DEVELOPMENT (DEV) mode.");
            logger.info("Database: H2 (in-memory)");
            logger.info("Logging level: DEBUG");
            logger.info("Swagger UI: http://localhost:8080/swagger-ui.html");
            logger.info("========================================");
        }
    }

    @Configuration
    @Profile("prod")
    static class ProdProfile {
        @PostConstruct
        public void init() {
            logger.info("========================================");
            logger.info("CryptoVault operates in PRODUCTION (PROD) mode.");
            logger.info("Database: PostgreSQL");
            logger.info("Logging level: INFO");
            logger.info("Security: HTTPS only");
            logger.info("========================================");
        }
    }
}