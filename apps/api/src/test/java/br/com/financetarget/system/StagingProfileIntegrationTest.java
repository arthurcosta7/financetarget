package br.com.financetarget.system;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("staging")
@Testcontainers
class StagingProfileIntegrationTest {
    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:17.11-alpine")
            .withDatabaseName("financetarget_staging_test")
            .withUsername("financetarget_staging_test")
            .withPassword("synthetic-staging-password");

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("app.environment", () -> "staging");
        registry.add("app.product.default-currency", () -> "BRL");
        registry.add("app.product.business-time-zone", () -> "America/Sao_Paulo");
        registry.add("app.cors.allowed-origins", () -> "https://staging.example.test");
        registry.add("app.auth.secure-cookies", () -> "true");
        registry.add("app.legal-documents.terms-version", () -> "staging-review-v1");
        registry.add("app.legal-documents.privacy-notice-version", () -> "staging-review-v1");
    }

    @Autowired JdbcClient jdbc;

    @Test
    void startsFailClosedWithoutDevelopmentSeeds() {
        assertThat(jdbc.sql("select metadata_value from app_metadata where metadata_key='schema_version'")
                .query(String.class).single()).isEqualTo("6");
        assertThat(jdbc.sql("select count(*) from subscription_plan").query(Integer.class).single()).isZero();
    }
}
