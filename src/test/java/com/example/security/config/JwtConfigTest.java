package com.example.security.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Configuration;

class JwtConfigTest {

    @Configuration
    @EnableConfigurationProperties(JwtConfig.class)
    static class TestConfiguration {
    }

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(TestConfiguration.class))
            .withPropertyValues(
                "jwt.expiration=3600000",
                "jwt.refresh-expiration=86400000");

    @Test
    void jwtConfig_shouldLoadPropertiesCorrectly() {
        this.contextRunner
            .run(context -> {
                JwtConfig jwtConfig = context.getBean(JwtConfig.class);

                assertNotNull(jwtConfig);
                assertEquals(3600000, jwtConfig.getExpiration());
                assertEquals(86400000, jwtConfig.getRefreshExpiration());
            });
    }
}