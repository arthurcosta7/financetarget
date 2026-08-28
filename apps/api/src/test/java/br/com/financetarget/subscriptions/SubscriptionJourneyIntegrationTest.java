package br.com.financetarget.subscriptions;

import br.com.financetarget.identity.infrastructure.messaging.InMemoryIdentityMessageAdapter;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@ActiveProfiles("test")
class SubscriptionJourneyIntegrationTest {
    private static final String PASSWORD = "uma senha longa e segura";
    private static final String WEBHOOK_SECRET = "synthetic-test-secret-with-32-characters";

    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:17.11-alpine")
            .withDatabaseName("financetarget_subscription_test")
            .withUsername("financetarget_test")
            .withPassword("synthetic-test-password");

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("app.environment", () -> "test");
        registry.add("app.product.default-currency", () -> "BRL");
        registry.add("app.product.business-time-zone", () -> "America/Sao_Paulo");
        registry.add("app.cors.allowed-origins", () -> "http://localhost:3000");
        registry.add("app.auth.secure-cookies", () -> "false");
        registry.add("app.legal-documents.terms-version", () -> "test-terms-v1");
        registry.add("app.legal-documents.privacy-notice-version", () -> "test-privacy-v1");
        registry.add("app.features.payments-mock", () -> "true");
        registry.add("app.features.notifications-mock", () -> "true");
        registry.add("app.mock-integrations.payment-webhook-secret", () -> WEBHOOK_SECRET);
    }

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    @Autowired JdbcClient jdbc;
    @Autowired InMemoryIdentityMessageAdapter messages;

    @BeforeEach
    void clean() {
        jdbc.sql("""
                truncate notification_intent,notification_preference,payment_webhook_event,checkout_session,
                account_subscription,plan_entitlement,subscription_plan,audit_event,data_subject_request,
                consent_record,financial_profile,space_member,planning_space,access_token,refresh_token,
                session_family,identity_token,credential,app_user cascade
                """).update();
        jdbc.sql("""
                insert into subscription_plan(code,display_name,status,created_at)
                values ('TEST_COMPLETE','Completo sintético','ACTIVE',current_timestamp)
                """).update();
        jdbc.sql("""
                insert into plan_entitlement(plan_code,entitlement_key,entitlement_value)
                values ('TEST_COMPLETE','SCENARIO_LIMIT','3'),
                       ('TEST_COMPLETE','SHARED_PLANNING','enabled')
                """).update();
        messages.clear();
    }

    @Test
    void createsMockCheckoutExactlyOnceAndKeepsEntitlementsInsideTheCatalog() throws Exception {
        Cookie access = verifiedAccess("ana.subscription@example.test", "Ana");
        String payload = json.writeValueAsString(Map.of("planCode", "TEST_COMPLETE"));
        MvcResult first = mvc.perform(withCsrf(post("/api/v1/subscriptions/mock-checkouts").cookie(access)
                        .header("Idempotency-Key", "checkout-1").contentType(MediaType.APPLICATION_JSON)
                        .content(payload))).andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("SIMULATED")).andReturn();
        MvcResult repeated = mvc.perform(withCsrf(post("/api/v1/subscriptions/mock-checkouts").cookie(access)
                        .header("Idempotency-Key", "checkout-1").contentType(MediaType.APPLICATION_JSON)
                        .content(payload))).andExpect(status().isCreated()).andReturn();

        assertThat(json.readTree(first.getResponse().getContentAsString()).path("id"))
                .isEqualTo(json.readTree(repeated.getResponse().getContentAsString()).path("id"));
        assertThat(jdbc.sql("select count(*) from checkout_session").query(Integer.class).single()).isEqualTo(1);
        mvc.perform(withCsrf(post("/api/v1/subscriptions/mock-checkouts").cookie(access)
                        .header("Idempotency-Key", "checkout-1").contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(Map.of("planCode", "ANOTHER")))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("IDEMPOTENCY_KEY_REUSED"));
    }

    @Test
    void verifiesWebhookBeforeParsingAndProcessesAReplayOnlyOnce() throws Exception {
        Cookie access = verifiedAccess("bia.subscription@example.test", "Bia");
        UUID userId = jdbc.sql("select id from app_user where email_normalized=:email")
                .param("email", "bia.subscription@example.test").query(UUID.class).single();
        String body = json.writeValueAsString(Map.of("eventType", "subscription.activated", "userId", userId,
                "planCode", "TEST_COMPLETE", "providerSubscriptionReference", "mock-subscription-1"));
        String timestamp = Instant.now().toString();

        mvc.perform(post("/api/v1/integrations/payments/webhooks/mock")
                        .header("X-Mock-Event-Id", "event-1").header("X-Mock-Timestamp", timestamp)
                        .header("X-Mock-Signature", "invalid").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isUnauthorized());
        String signature = signature(timestamp, body);
        for (int attempt = 0; attempt < 2; attempt++) {
            mvc.perform(post("/api/v1/integrations/payments/webhooks/mock")
                            .header("X-Mock-Event-Id", "event-1").header("X-Mock-Timestamp", timestamp)
                            .header("X-Mock-Signature", signature).contentType(MediaType.APPLICATION_JSON).content(body))
                    .andExpect(status().isAccepted());
        }

        assertThat(jdbc.sql("select count(*) from payment_webhook_event").query(Integer.class).single()).isEqualTo(1);
        assertThat(jdbc.sql("select count(*) from account_subscription").query(Integer.class).single()).isEqualTo(1);
        assertThat(jdbc.sql("select count(*) from notification_intent").query(Integer.class).single()).isEqualTo(1);
        mvc.perform(get("/api/v1/subscriptions/current").cookie(access)).andExpect(status().isOk())
                .andExpect(jsonPath("$.subscription.status").value("ACTIVE"))
                .andExpect(jsonPath("$.entitlements.SCENARIO_LIMIT").value("3"));
    }

    @Test
    void rejectsExpiredWebhooksAndEventIdentifiersReusedWithDifferentContent() throws Exception {
        verifiedAccess("dani.webhook@example.test", "Dani");
        UUID userId = jdbc.sql("select id from app_user where email_normalized=:email")
                .param("email", "dani.webhook@example.test").query(UUID.class).single();
        String original = json.writeValueAsString(Map.of("eventType", "subscription.activated", "userId", userId,
                "planCode", "TEST_COMPLETE", "providerSubscriptionReference", "mock-subscription-security"));
        String expiredTimestamp = Instant.now().minusSeconds(600).toString();

        mvc.perform(post("/api/v1/integrations/payments/webhooks/mock")
                        .header("X-Mock-Event-Id", "event-security")
                        .header("X-Mock-Timestamp", expiredTimestamp)
                        .header("X-Mock-Signature", signature(expiredTimestamp, original))
                        .contentType(MediaType.APPLICATION_JSON).content(original))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.title").value("WEBHOOK_REPLAY_WINDOW_EXCEEDED"));

        String acceptedTimestamp = Instant.now().toString();
        mvc.perform(post("/api/v1/integrations/payments/webhooks/mock")
                        .header("X-Mock-Event-Id", "event-security")
                        .header("X-Mock-Timestamp", acceptedTimestamp)
                        .header("X-Mock-Signature", signature(acceptedTimestamp, original))
                        .contentType(MediaType.APPLICATION_JSON).content(original))
                .andExpect(status().isAccepted());

        String changed = json.writeValueAsString(Map.of("eventType", "subscription.canceled", "userId", userId,
                "planCode", "TEST_COMPLETE", "providerSubscriptionReference", "mock-subscription-security"));
        String changedTimestamp = Instant.now().toString();
        mvc.perform(post("/api/v1/integrations/payments/webhooks/mock")
                        .header("X-Mock-Event-Id", "event-security")
                        .header("X-Mock-Timestamp", changedTimestamp)
                        .header("X-Mock-Signature", signature(changedTimestamp, changed))
                        .contentType(MediaType.APPLICATION_JSON).content(changed))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("WEBHOOK_EVENT_REUSED"));

        assertThat(jdbc.sql("select status from account_subscription where user_id=:userId")
                .param("userId", userId).query(String.class).single()).isEqualTo("ACTIVE");
        assertThat(jdbc.sql("select count(*) from payment_webhook_event").query(Integer.class).single()).isEqualTo(1);
    }

    @Test
    void persistsOptionalPreferencesWhileEssentialMessagesRemainEnabled() throws Exception {
        Cookie access = verifiedAccess("caio.preferences@example.test", "Caio");
        String preferences = json.writeValueAsString(Map.of(
                "planningReminders", true, "productUpdates", false, "marketing", true));
        mvc.perform(withCsrf(put("/api/v1/notification-preferences").cookie(access)
                        .contentType(MediaType.APPLICATION_JSON).content(preferences)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.essential").value(true))
                .andExpect(jsonPath("$.planningReminders").value(true))
                .andExpect(jsonPath("$.marketing").value(true));
        assertThat(jdbc.sql("select count(*) from notification_preference where email_enabled=true")
                .query(Integer.class).single()).isEqualTo(2);
    }

    private Cookie verifiedAccess(String email, String name) throws Exception {
        mvc.perform(withCsrf(post("/api/v1/auth/registrations").contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(Map.of("email", email, "displayName", name,
                                "password", PASSWORD))))).andExpect(status().isAccepted());
        mvc.perform(withCsrf(post("/api/v1/auth/verifications").contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(Map.of("token", messages.latestFor(email).token())))))
                .andExpect(status().isNoContent());
        MvcResult login = mvc.perform(withCsrf(post("/api/v1/auth/sessions").contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(Map.of("email", email, "password", PASSWORD)))))
                .andExpect(status().isNoContent()).andReturn();
        return sessionCookie(login, "ft_access");
    }

    private Cookie sessionCookie(MvcResult result, String name) {
        List<String> headers = result.getResponse().getHeaders("Set-Cookie");
        String raw = headers.stream().filter(value -> value.startsWith(name + "=")).findFirst().orElseThrow();
        return new Cookie(name, raw.substring(name.length() + 1, raw.indexOf(';')));
    }

    private MockHttpServletRequestBuilder withCsrf(MockHttpServletRequestBuilder request) throws Exception {
        MvcResult bootstrap = mvc.perform(get("/api/v1/auth/csrf")).andExpect(status().isOk()).andReturn();
        Cookie cookie = bootstrap.getResponse().getCookie("XSRF-TOKEN");
        assertThat(cookie).isNotNull();
        return request.cookie(cookie).header("X-XSRF-TOKEN", cookie.getValue());
    }

    private String signature(String timestamp, String body) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(WEBHOOK_SECRET.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        return HexFormat.of().formatHex(mac.doFinal((timestamp + "." + body).getBytes(StandardCharsets.UTF_8)));
    }
}
