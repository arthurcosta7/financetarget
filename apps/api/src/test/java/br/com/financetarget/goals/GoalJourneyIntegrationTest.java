package br.com.financetarget.goals;

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
class GoalJourneyIntegrationTest {
    private static final String PASSWORD = "uma senha longa e segura";

    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:17.11-alpine")
            .withDatabaseName("financetarget_goal_test")
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

    @BeforeEach
    void clean() {
        jdbc.sql("""
                truncate audit_event,data_subject_request,consent_record,financial_profile,space_member,
                planning_space,access_token,refresh_token,session_family,identity_token,credential,app_user cascade
                """).update();
        messages.clear();
    }

    @Test
    void createsReproducibleGoalSnapshotAndKeepsItInsideTheSpace() throws Exception {
        Cookie ana = verifiedAccess("ana.goal@example.test", "Ana");
        Cookie bia = verifiedAccess("bia.goal@example.test", "Bia");
        UUID anaSpace = personalSpace("ana.goal@example.test");
        String targetDate = LocalDate.now(ZoneId.of("America/Sao_Paulo")).plusYears(4).toString();

        MvcResult created = mvc.perform(withCsrf(post("/api/v1/planning-spaces/" + anaSpace + "/goals")
                        .cookie(ana).contentType(MediaType.APPLICATION_JSON).content(goalBody(targetDate))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.projection.requiredMonthlyContribution.amount").value("2000.00"))
                .andExpect(jsonPath("$.projection.projectionMonths").value(48))
                .andExpect(jsonPath("$.projection.engineVersion").value("goal-engine-1"))
                .andReturn();
        JsonNode body = json.readTree(created.getResponse().getContentAsString());
        UUID goalId = UUID.fromString(body.path("id").asText());

        assertThat(jdbc.sql("select count(*) from calculation_snapshot where goal_id=:goalId")
                .param("goalId", goalId).query(Integer.class).single()).isEqualTo(1);
        assertThat(jdbc.sql("select input_hash from calculation_snapshot where goal_id=:goalId")
                .param("goalId", goalId).query(String.class).single()).hasSize(64);

        mvc.perform(get("/api/v1/planning-spaces/" + anaSpace + "/goals/" + goalId).cookie(bia))
                .andExpect(status().isNotFound());
        mvc.perform(post("/api/v1/planning-spaces/" + anaSpace + "/goals").cookie(ana)
                        .contentType(MediaType.APPLICATION_JSON).content(goalBody(targetDate)))
                .andExpect(status().isForbidden());
    }

    @Test
    void recordsContributionExactlyOnceAndRejectsKeyReuseWithDifferentPayload() throws Exception {
        Cookie access = verifiedAccess("caio.goal@example.test", "Caio");
        UUID spaceId = personalSpace("caio.goal@example.test");
        String targetDate = LocalDate.now(ZoneId.of("America/Sao_Paulo")).plusYears(4).toString();
        MvcResult created = mvc.perform(withCsrf(post("/api/v1/planning-spaces/" + spaceId + "/goals")
                        .cookie(access).contentType(MediaType.APPLICATION_JSON).content(goalBody(targetDate))))
                .andExpect(status().isCreated()).andReturn();
        String goalId = json.readTree(created.getResponse().getContentAsString()).path("id").asText();
        String endpoint = "/api/v1/planning-spaces/" + spaceId + "/goals/" + goalId + "/contributions";
        String date = LocalDate.now(ZoneId.of("America/Sao_Paulo")).toString();
        String firstPayload = json.writeValueAsString(Map.of("amount", "500.00", "contributionDate", date,
                "note", "Aporte sintético"));

        MvcResult first = mvc.perform(withCsrf(post(endpoint).cookie(access).header("Idempotency-Key", "goal-contribution-1")
                        .contentType(MediaType.APPLICATION_JSON).content(firstPayload)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.goal.currentBalance.amount").value("24500.00"))
                .andReturn();
        MvcResult repeated = mvc.perform(withCsrf(post(endpoint).cookie(access).header("Idempotency-Key", "goal-contribution-1")
                        .contentType(MediaType.APPLICATION_JSON).content(firstPayload)))
                .andExpect(status().isCreated()).andReturn();

        assertThat(json.readTree(first.getResponse().getContentAsString()).path("contribution").path("id"))
                .isEqualTo(json.readTree(repeated.getResponse().getContentAsString()).path("contribution").path("id"));
        assertThat(jdbc.sql("select count(*) from contribution").query(Integer.class).single()).isEqualTo(1);

        String conflicting = json.writeValueAsString(Map.of("amount", "600.00", "contributionDate", date));
        mvc.perform(withCsrf(post(endpoint).cookie(access).header("Idempotency-Key", "goal-contribution-1")
                        .contentType(MediaType.APPLICATION_JSON).content(conflicting)))
                .andExpect(status().isConflict()).andExpect(jsonPath("$.title").value("IDEMPOTENCY_KEY_REUSED"));

        mvc.perform(withCsrf(post("/api/v1/privacy/exports").cookie(access)
                        .header("Idempotency-Key", "goal-export-1").contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(Map.of("password", PASSWORD)))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.goals[0].title").value("Entrada do imóvel"))
                .andExpect(jsonPath("$.goals[0].contributions[0].amount").value("500.00"))
                .andExpect(jsonPath("$.goals[0].formulaVersion").value("monthly-annuity-1"));
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

    private UUID personalSpace(String email) {
        return jdbc.sql("""
                        select s.id from planning_space s join space_member m on m.space_id=s.id
                        join app_user u on u.id=m.user_id where u.email_normalized=:email
                        """).param("email", email).query(UUID.class).single();
    }

    private String goalBody(String targetDate) throws Exception {
        return json.writeValueAsString(Map.of("goalType", "HOME_DOWN_PAYMENT", "title", "Entrada do imóvel",
                "targetAmount", "120000.00", "targetValueBasis", "FIXED_NOMINAL", "targetDate", targetDate,
                "initialBalance", "24000.00", "annualInflationRate", "0", "annualReturnRate", "0",
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
