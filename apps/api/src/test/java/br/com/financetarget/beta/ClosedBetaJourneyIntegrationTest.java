package br.com.financetarget.beta;

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
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDate;
import java.time.ZoneId;
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
class ClosedBetaJourneyIntegrationTest {
    private static final String PASSWORD = "uma senha longa e segura";

    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:17.11-alpine")
            .withDatabaseName("financetarget_beta_test").withUsername("financetarget_test")
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
        registry.add("app.beta.enabled", () -> "true");
    }

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    @Autowired JdbcClient jdbc;
    @Autowired InMemoryIdentityMessageAdapter messages;

    @BeforeEach
    void clean() {
        jdbc.sql("""
                truncate beta_feedback,beta_product_event,space_invitation,authentication_attempt_window,
                audit_event,data_subject_request,consent_record,financial_profile,space_member,planning_space,
                access_token,refresh_token,session_family,identity_token,credential,app_user cascade
                """).update();
        messages.clear();
    }

    @Test
    void sharesSpaceOnlyWithTheBoundRecipientAndEnforcesRoles() throws Exception {
        Cookie owner = verifiedAccess("owner.beta@example.test", "Owner");
        Cookie partner = verifiedAccess("partner.beta@example.test", "Partner");
        Cookie outsider = verifiedAccess("outsider.beta@example.test", "Outsider");

        MvcResult created = mvc.perform(withCsrf(post("/api/v1/planning-spaces").cookie(owner)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"Plano do casal\"}")))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.type").value("SHARED"))
                .andExpect(jsonPath("$.role").value("OWNER")).andReturn();
        String spaceId = json.readTree(created.getResponse().getContentAsString()).path("id").asText();

        MvcResult invited = mvc.perform(withCsrf(post("/api/v1/planning-spaces/" + spaceId + "/invitations")
                        .cookie(owner).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"partner.beta@example.test\",\"role\":\"EDITOR\"}")))
                .andExpect(status().isAccepted()).andExpect(jsonPath("$.role").value("EDITOR")).andReturn();
        String invitationId = json.readTree(invited.getResponse().getContentAsString()).path("id").asText();

        mvc.perform(withCsrf(post("/api/v1/planning-space-invitations/" + invitationId + "/responses")
                        .cookie(outsider).contentType(MediaType.APPLICATION_JSON).content("{\"accept\":true}")))
                .andExpect(status().isNotFound());
        mvc.perform(get("/api/v1/planning-space-invitations").cookie(partner))
                .andExpect(status().isOk()).andExpect(jsonPath("$[0].spaceName").value("Plano do casal"));
        mvc.perform(withCsrf(post("/api/v1/planning-space-invitations/" + invitationId + "/responses")
                        .cookie(partner).contentType(MediaType.APPLICATION_JSON).content("{\"accept\":true}")))
                .andExpect(status().isNoContent());

        mvc.perform(withCsrf(put("/api/v1/planning-spaces/" + spaceId + "/financial-profile").cookie(partner)
                        .contentType(MediaType.APPLICATION_JSON).content("""
                                {"recurringIncome":"10000.00","essentialExpenses":"6000.00",
                                 "initialGoalBalance":"20000.00","confirmedMonthlyCapacity":"3500.00"}
                                """))).andExpect(status().isOk())
                .andExpect(jsonPath("$.suggestedMonthlyCapacity").value("4000.00"));

        String targetDate = LocalDate.now(ZoneId.of("America/Sao_Paulo")).plusYears(3).toString();
        mvc.perform(withCsrf(post("/api/v1/planning-spaces/" + spaceId + "/goals").cookie(partner)
                        .contentType(MediaType.APPLICATION_JSON).content(goalBody(targetDate))))
                .andExpect(status().isCreated());

        UUID partnerId = userId("partner.beta@example.test");
        mvc.perform(withCsrf(patch("/api/v1/planning-spaces/" + spaceId + "/members/" + partnerId).cookie(owner)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"role\":\"VIEWER\"}")))
                .andExpect(status().isNoContent());
        mvc.perform(withCsrf(post("/api/v1/planning-spaces/" + spaceId + "/goals").cookie(partner)
                        .contentType(MediaType.APPLICATION_JSON).content(goalBody(targetDate))))
                .andExpect(status().isNotFound());
        mvc.perform(get("/api/v1/planning-spaces/" + spaceId + "/financial-profile").cookie(partner))
                .andExpect(status().isOk());
        mvc.perform(get("/api/v1/planning-spaces/" + spaceId + "/members").cookie(outsider))
                .andExpect(status().isNotFound());

        UUID ownerId = userId("owner.beta@example.test");
        mvc.perform(withCsrf(patch("/api/v1/planning-spaces/" + spaceId + "/members/" + ownerId).cookie(owner)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"role\":\"VIEWER\"}")))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.title").value("LAST_OWNER_REQUIRED"));

        MvcResult partnerExport = mvc.perform(withCsrf(post("/api/v1/privacy/exports").cookie(partner)
                        .header("Idempotency-Key", "beta-partner-export-1").contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(Map.of("password", PASSWORD)))))
                .andExpect(status().isOk()).andReturn();
        String partnerExportBody = partnerExport.getResponse().getContentAsString();
        assertThat(partnerExportBody).contains(spaceId).doesNotContain("owner.beta@example.test");
    }

    @Test
    void storesOnlyAllowlistedLearningDataAndStructuredFeedback() throws Exception {
        Cookie access = verifiedAccess("learning.beta@example.test", "Learning");
        mvc.perform(get("/api/v1/beta/config").cookie(access)).andExpect(status().isOk())
                .andExpect(jsonPath("$.enabled").value(true))
                .andExpect(jsonPath("$.maximumSharedMembers").value(2));
        mvc.perform(withCsrf(post("/api/v1/beta/events").cookie(access).contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"eventName":"DASHBOARD_VIEWED","journeyStage":"ACTIVATION",
                                 "outcome":"COMPLETED","deviceClass":"DESKTOP"}
                                """))).andExpect(status().isAccepted());
        mvc.perform(withCsrf(post("/api/v1/beta/events").cookie(access).contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"eventName":"BALANCE_CAPTURED","journeyStage":"ACTIVATION",
                                 "outcome":"COMPLETED","deviceClass":"DESKTOP"}
                                """))).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("INVALID_BETA_EVENT"));
        mvc.perform(withCsrf(post("/api/v1/beta/events").cookie(access).contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"eventName":"DASHBOARD_VIEWED","journeyStage":"ACTIVATION",
                                 "outcome":"COMPLETED","deviceClass":"DESKTOP","balance":"90000.00"}
                                """))).andExpect(status().isBadRequest());

        mvc.perform(withCsrf(post("/api/v1/beta/feedback").cookie(access).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"category\":\"TRUST\",\"rating\":4,\"comment\":\"Explicação sintética clara.\"}")))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.status").value("OPEN"));

        assertThat(jdbc.sql("select event_name from beta_product_event").query(String.class).single())
                .isEqualTo("DASHBOARD_VIEWED");
        assertThat(jdbc.sql("select count(*) from beta_product_event").query(Integer.class).single()).isEqualTo(1);
        assertThat(jdbc.sql("select comment from beta_feedback").query(String.class).single())
                .isEqualTo("Explicação sintética clara.");

        mvc.perform(withCsrf(post("/api/v1/privacy/exports").cookie(access)
                        .header("Idempotency-Key", "beta-learning-export-1").contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(Map.of("password", PASSWORD)))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.betaEvents[0].eventName").value("DASHBOARD_VIEWED"))
                .andExpect(jsonPath("$.betaFeedback[0].category").value("TRUST"))
                .andExpect(jsonPath("$.betaFeedback[0].comment").value("Explicação sintética clara."));
    }

    private Cookie verifiedAccess(String email, String name) throws Exception {
        mvc.perform(withCsrf(post("/api/v1/auth/registrations").contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(Map.of("email", email, "displayName", name, "password", PASSWORD)))))
                .andExpect(status().isAccepted());
        mvc.perform(withCsrf(post("/api/v1/auth/verifications").contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(Map.of("token", messages.latestFor(email).token())))))
                .andExpect(status().isNoContent());
        MvcResult login = mvc.perform(withCsrf(post("/api/v1/auth/sessions").contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(Map.of("email", email, "password", PASSWORD)))))
                .andExpect(status().isNoContent()).andReturn();
        return sessionCookie(login, "ft_access");
    }

    private UUID userId(String email) {
        return jdbc.sql("select id from app_user where email_normalized=:email")
                .param("email", email).query(UUID.class).single();
    }

    private String goalBody(String targetDate) throws Exception {
        return json.writeValueAsString(Map.of("goalType", "HOME_DOWN_PAYMENT", "title", "Meta compartilhada",
                "targetAmount", "120000.00", "targetValueBasis", "FIXED_NOMINAL", "targetDate", targetDate,
                "initialBalance", "20000.00", "annualInflationRate", "0", "annualReturnRate", "0",
                "contributionTiming", "END_OF_MONTH"));
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
}
