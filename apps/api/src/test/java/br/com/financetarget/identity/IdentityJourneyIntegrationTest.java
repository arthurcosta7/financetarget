package br.com.financetarget.identity;

import br.com.financetarget.identity.infrastructure.messaging.InMemoryIdentityMessageAdapter;
import com.fasterxml.jackson.databind.JsonNode;
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
import org.springframework.security.web.FilterChainProxy;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfFilter;
import org.springframework.test.util.ReflectionTestUtils;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@ActiveProfiles("test")
class IdentityJourneyIntegrationTest {
    private static final String PASSWORD = "uma senha longa e segura";

    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:17.11-alpine")
            .withDatabaseName("financetarget_identity_test")
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
    }

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    @Autowired JdbcClient jdbc;
    @Autowired InMemoryIdentityMessageAdapter messages;
    @Autowired FilterChainProxy securityFilters;

    @BeforeEach
    void clean() {
        jdbc.sql("""
                truncate authentication_attempt_window,audit_event,data_subject_request,consent_record,financial_profile,space_member,
                planning_space,access_token,refresh_token,session_family,identity_token,credential,app_user cascade
                """).update();
        messages.clear();
    }

    @Test
    void protectsMutationsWithCsrfAndCreatesExactlyOnePersonalSpaceAfterOneUseVerification() throws Exception {
        mvc.perform(post("/api/v1/auth/registrations").contentType(MediaType.APPLICATION_JSON)
                        .content(registration("ana@example.test", "Ana")))
                .andExpect(status().isForbidden());

        register("ana@example.test", "Ana");
        String token = messages.latestFor("ana@example.test").token();
        verify(token).andExpect(status().isNoContent());
        verify(token).andExpect(status().isBadRequest()).andExpect(jsonPath("$.title").value("INVALID_OR_EXPIRED_TOKEN"));

        assertThat(jdbc.sql("select count(*) from planning_space").query(Integer.class).single()).isEqualTo(1);
        assertThat(jdbc.sql("select count(*) from space_member where role='OWNER'").query(Integer.class).single()).isEqualTo(1);
    }

    @Test
    void keepsRegistrationAndRecoveryResponsesGeneric() throws Exception {
        String first = register("bia@example.test", "Bia").andReturn().getResponse().getContentAsString();
        String duplicate = register("bia@example.test", "Outra pessoa").andReturn().getResponse().getContentAsString();
        assertThat(duplicate).isEqualTo(first);

        String existing = recoverRequest("bia@example.test").andReturn().getResponse().getContentAsString();
        String absent = recoverRequest("ausente@example.test").andReturn().getResponse().getContentAsString();
        assertThat(absent).isEqualTo(existing);
    }

    @Test
    void rotatesRefreshAndRevokesFamilyWhenAnOldTokenIsReused() throws Exception {
        Cookie[] session = verifiedSession("caio@example.test", "Caio");
        Cookie oldRefresh = session[1];

        MvcResult rotated = mvc.perform(withCsrf(post("/api/v1/auth/sessions/refresh").cookie(oldRefresh)))
                .andExpect(status().isNoContent()).andReturn();
        Cookie newAccess = sessionCookie(rotated, "ft_access");

        mvc.perform(withCsrf(post("/api/v1/auth/sessions/refresh").cookie(oldRefresh)))
                .andExpect(status().isUnauthorized());
        mvc.perform(get("/api/v1/auth/me").cookie(newAccess)).andExpect(status().isUnauthorized());
    }

    @Test
    void isolatesFinancialProfileAndExportBetweenUsersAndKeepsMoneyExact() throws Exception {
        Cookie[] ana = verifiedSession("ana@example.test", "Ana");
        Cookie[] bia = verifiedSession("bia@example.test", "Bia");

        mvc.perform(withCsrf(put("/api/v1/onboarding/financial-profile").cookie(ana[0])
                        .contentType(MediaType.APPLICATION_JSON).content("""
                                {"recurringIncome": "5000.10", "essentialExpenses": "3200.05",
                                 "initialGoalBalance": "150.00", "confirmedMonthlyCapacity": "1700.00",
                                 "termsAccepted": true, "privacyNoticeAcknowledged": true}
                                """)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.suggestedMonthlyCapacity").value("1800.05"));

        mvc.perform(get("/api/v1/onboarding/financial-profile").cookie(bia[0])).andExpect(status().isNotFound());

        MvcResult exported = mvc.perform(withCsrf(post("/api/v1/privacy/exports").cookie(ana[0])
                        .header("Idempotency-Key", "export-ana-1").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"password\":\"" + PASSWORD + "\"}")))
                .andExpect(status().isOk()).andReturn();
        JsonNode body = json.readTree(exported.getResponse().getContentAsString());
        assertThat(body.path("account").path("email").asText()).isEqualTo("ana@example.test");
        assertThat(body.toString()).doesNotContain("bia@example.test", "password", "token");
    }

    @Test
    void recoversPasswordOnceRevokesSessionsAndMakesDeletionIdempotent() throws Exception {
        Cookie[] session = verifiedSession("dora@example.test", "Dora");
        recoverRequest("dora@example.test");
        String recoveryToken = messages.latestFor("dora@example.test").token();

        mvc.perform(withCsrf(post("/api/v1/auth/password-recoveries").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"token\":\"" + recoveryToken + "\",\"newPassword\":\"nova senha muito longa e segura\"}")))
                .andExpect(status().isNoContent());
        mvc.perform(withCsrf(post("/api/v1/auth/password-recoveries").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"token\":\"" + recoveryToken + "\",\"newPassword\":\"outra senha muito longa segura\"}")))
                .andExpect(status().isBadRequest());
        mvc.perform(get("/api/v1/auth/me").cookie(session[0])).andExpect(status().isUnauthorized());

        Cookie[] renewed = login("dora@example.test", "nova senha muito longa e segura");
        String requestBody = "{\"password\":\"nova senha muito longa e segura\"}";
        MvcResult first = mvc.perform(withCsrf(post("/api/v1/privacy/deletion-requests").cookie(renewed[0])
                        .header("Idempotency-Key", "delete-dora-1").contentType(MediaType.APPLICATION_JSON).content(requestBody)))
                .andExpect(status().isAccepted()).andReturn();
        MvcResult repeated = mvc.perform(withCsrf(post("/api/v1/privacy/deletion-requests").cookie(renewed[0])
                        .header("Idempotency-Key", "delete-dora-1").contentType(MediaType.APPLICATION_JSON).content(requestBody)))
                .andExpect(status().isAccepted()).andReturn();
        assertThat(json.readTree(first.getResponse().getContentAsString()).path("id"))
                .isEqualTo(json.readTree(repeated.getResponse().getContentAsString()).path("id"));
    }

    @Test
    void issuesHttpOnlySameSiteCookiesAndCsrfBootstrapCookie() throws Exception {
        CsrfFilter csrfFilter = securityFilters.getFilters("/api/v1/auth/csrf").stream()
                .filter(CsrfFilter.class::isInstance).map(CsrfFilter.class::cast).findFirst().orElseThrow();
        assertThat(ReflectionTestUtils.getField(csrfFilter, "tokenRepository"))
                .isInstanceOf(CookieCsrfTokenRepository.class);
        mvc.perform(get("/api/v1/auth/csrf"))
                .andExpect(status().isOk()).andExpect(cookie().exists("XSRF-TOKEN"));
        Cookie[] cookies = verifiedSession("eva@example.test", "Eva");
        assertThat(cookies).hasSize(2);
    }

    @Test
    void persistsAuthenticationRateLimitsWithoutStoringTheIdentifier() throws Exception {
        for (int attempt = 0; attempt < 10; attempt++) {
            mvc.perform(withCsrf(post("/api/v1/auth/sessions").contentType(MediaType.APPLICATION_JSON)
                            .content(json.writeValueAsString(java.util.Map.of(
                                    "email", "limit@example.test", "password", PASSWORD)))))
                    .andExpect(status().isUnauthorized());
        }
        mvc.perform(withCsrf(post("/api/v1/auth/sessions").contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(java.util.Map.of(
                                "email", "limit@example.test", "password", PASSWORD)))))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.title").value("RATE_LIMITED"));

        String storedKey = jdbc.sql("select key_hash from authentication_attempt_window")
                .query(String.class).single();
        assertThat(storedKey).hasSize(64).doesNotContain("limit@example.test");
    }

    private org.springframework.test.web.servlet.ResultActions register(String email, String name) throws Exception {
        return mvc.perform(withCsrf(post("/api/v1/auth/registrations").contentType(MediaType.APPLICATION_JSON)
                .content(registration(email, name)))).andExpect(status().isAccepted());
    }

    private String registration(String email, String name) throws Exception {
        return json.writeValueAsString(java.util.Map.of("email", email, "displayName", name, "password", PASSWORD));
    }

    private org.springframework.test.web.servlet.ResultActions verify(String token) throws Exception {
        return mvc.perform(withCsrf(post("/api/v1/auth/verifications").contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(java.util.Map.of("token", token)))));
    }

    private org.springframework.test.web.servlet.ResultActions recoverRequest(String email) throws Exception {
        return mvc.perform(withCsrf(post("/api/v1/auth/password-recovery-requests")
                .contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(java.util.Map.of("email", email)))))
                .andExpect(status().isAccepted());
    }

    private Cookie[] verifiedSession(String email, String name) throws Exception {
        register(email, name);
        verify(messages.latestFor(email).token()).andExpect(status().isNoContent());
        return login(email, PASSWORD);
    }

    private Cookie[] login(String email, String password) throws Exception {
        MvcResult result = mvc.perform(withCsrf(post("/api/v1/auth/sessions").contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(java.util.Map.of("email", email, "password", password)))))
                .andExpect(status().isNoContent())
                .andReturn();
        return new Cookie[]{sessionCookie(result, "ft_access"), sessionCookie(result, "ft_refresh")};
    }

    private Cookie sessionCookie(MvcResult result, String name) {
        List<String> headers = result.getResponse().getHeaders("Set-Cookie");
        String raw = headers.stream().filter(value -> value.startsWith(name + "=")).findFirst().orElseThrow();
        String value = raw.substring(name.length() + 1, raw.indexOf(';'));
        assertThat(raw).contains("HttpOnly", "SameSite=Lax");
        return new Cookie(name, value);
    }

    private MockHttpServletRequestBuilder withCsrf(MockHttpServletRequestBuilder request) throws Exception {
        MvcResult bootstrap = mvc.perform(get("/api/v1/auth/csrf")).andExpect(status().isOk()).andReturn();
        Cookie cookie = bootstrap.getResponse().getCookie("XSRF-TOKEN");
        assertThat(cookie).isNotNull();
        return request.cookie(cookie).header("X-XSRF-TOKEN", cookie.getValue());
    }
}
