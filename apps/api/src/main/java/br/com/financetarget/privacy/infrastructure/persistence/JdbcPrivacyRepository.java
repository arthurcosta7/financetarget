package br.com.financetarget.privacy.infrastructure.persistence;

import br.com.financetarget.privacy.application.PrivacyRepository;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Repository
public class JdbcPrivacyRepository implements PrivacyRepository {
    private final JdbcClient jdbc;

    public JdbcPrivacyRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public ExportData exportOwnData(UUID userId) {
        AccountData account = jdbc.sql("""
                        select id,email_normalized,display_name,created_at,email_verified_at
                        from app_user where id=:userId
                        """).param("userId", userId).query((rs, row) -> new AccountData(
                        rs.getObject("id", UUID.class), rs.getString("email_normalized"), rs.getString("display_name"),
                        rs.getTimestamp("created_at").toInstant(), rs.getTimestamp("email_verified_at").toInstant())).single();

        Optional<FinancialData> profile = jdbc.sql("""
                        select p.* from financial_profile p
                        join planning_space s on s.id=p.space_id and s.type='PERSONAL'
                        join space_member m on m.space_id=s.id
                        where m.user_id=:userId and m.role='OWNER' and m.status='ACTIVE'
                        """).param("userId", userId).query((rs, row) -> new FinancialData(
                        rs.getBigDecimal("recurring_income").toPlainString(),
                        rs.getBigDecimal("essential_expenses").toPlainString(),
                        rs.getBigDecimal("initial_goal_balance").toPlainString(),
                        rs.getBigDecimal("suggested_monthly_capacity").toPlainString(),
                        rs.getBigDecimal("confirmed_monthly_capacity").toPlainString(),
                        rs.getString("currency"), rs.getDate("reference_date").toLocalDate().toString())).optional();

        var consents = jdbc.sql("""
                        select purpose,document_version,decision,recorded_at from consent_record
                        where user_id=:userId order by recorded_at
                        """).param("userId", userId).query((rs, row) -> new ConsentData(
                        rs.getString("purpose"), rs.getString("document_version"), rs.getString("decision"),
                        rs.getTimestamp("recorded_at").toInstant())).list();
        var goals = jdbc.sql("""
                        select g.*,s.engine_version,s.formula_version from goal g
                        join lateral (
                            select engine_version,formula_version from calculation_snapshot
                            where goal_id=g.id and scenario_id is null order by created_at desc,id desc limit 1
                        ) s on true
                        where g.created_by=:userId order by g.created_at,g.id
                        """).param("userId", userId).query((rs, row) -> {
                    UUID goalId = rs.getObject("id", UUID.class);
                    var contributions = jdbc.sql("""
                                    select * from contribution where goal_id=:goalId and created_by=:userId
                                    order by contribution_date,created_at,id
                                    """).param("goalId", goalId).param("userId", userId)
                            .query((crs, contributionRow) -> new ContributionData(crs.getObject("id", UUID.class),
                                    crs.getBigDecimal("amount").toPlainString(), crs.getString("currency"),
                                    crs.getDate("contribution_date").toLocalDate().toString(), crs.getString("note"),
                                    crs.getTimestamp("created_at").toInstant())).list();
                    var scenarios = jdbc.sql("""
                                    select sc.*,cs.engine_version,cs.formula_version from scenario sc
                                    join calculation_snapshot cs on cs.scenario_id=sc.id
                                    where sc.goal_id=:goalId and sc.created_by=:userId order by sc.created_at,sc.id
                                    """).param("goalId", goalId).param("userId", userId)
                            .query((srs, scenarioRow) -> new ScenarioData(srs.getObject("id", UUID.class),
                                    srs.getString("title"), srs.getDate("target_date").toLocalDate().toString(),
                                    srs.getBigDecimal("annual_inflation_rate").toPlainString(),
                                    srs.getBigDecimal("annual_return_rate").toPlainString(),
                                    srs.getString("contribution_timing"), srs.getString("engine_version"),
                                    srs.getString("formula_version"), srs.getTimestamp("created_at").toInstant())).list();
                    return new GoalData(goalId, rs.getString("title"), rs.getString("goal_type"),
                            rs.getBigDecimal("target_amount").toPlainString(), rs.getString("currency"),
                            rs.getString("target_value_basis"), rs.getDate("target_date").toLocalDate().toString(),
                            rs.getBigDecimal("initial_balance").toPlainString(),
                            rs.getBigDecimal("annual_inflation_rate").toPlainString(),
                            rs.getBigDecimal("annual_return_rate").toPlainString(), rs.getString("contribution_timing"),
                            rs.getBigDecimal("planned_monthly_contribution").toPlainString(), rs.getString("status"),
                            rs.getString("engine_version"), rs.getString("formula_version"),
                            rs.getTimestamp("created_at").toInstant(), contributions, scenarios);
                }).list();
        var subscription = jdbc.sql("""
                        select plan_code,status,provider,updated_at from account_subscription where user_id=:userId
                        """).param("userId", userId).query((rs, row) -> new SubscriptionData(
                        rs.getString("plan_code"), rs.getString("status"), rs.getString("provider"),
                        rs.getTimestamp("updated_at").toInstant())).optional();
        var preferences = jdbc.sql("""
                        select category,email_enabled,updated_at from notification_preference
                        where user_id=:userId order by category
                        """).param("userId", userId).query((rs, row) -> new NotificationPreferenceData(
                        rs.getString("category"), rs.getBoolean("email_enabled"),
                        rs.getTimestamp("updated_at").toInstant())).list();
        return new ExportData(account, profile, goals, consents, subscription, preferences);
    }

    @Override
    public Optional<SubjectRequest> findRequest(UUID userId, String type, String idempotencyKey) {
        return jdbc.sql("""
                        select id,request_type,status,created_at,completed_at from data_subject_request
                        where user_id=:userId and request_type=:type and idempotency_key=:key
                        """).param("userId", userId).param("type", type).param("key", idempotencyKey)
                .query(this::mapRequest).optional();
    }

    @Override
    public SubjectRequest createRequest(UUID userId, String type, String status, String idempotencyKey, Instant now) {
        UUID id = UUID.randomUUID();
        Instant completedAt = "COMPLETED".equals(status) ? now : null;
        jdbc.sql("""
                        insert into data_subject_request(id,user_id,request_type,status,idempotency_key,created_at,completed_at)
                        values (:id,:userId,:type,:status,:key,:now,:completedAt)
                        on conflict (user_id,request_type,idempotency_key) do nothing
                        """).param("id", id).param("userId", userId).param("type", type).param("status", status)
                .param("key", idempotencyKey).param("now", dbTime(now)).param("completedAt", dbTime(completedAt)).update();
        return findRequest(userId, type, idempotencyKey).orElseThrow();
    }

    @Override
    public Optional<SubjectRequest> latestDeletion(UUID userId) {
        return jdbc.sql("""
                        select id,request_type,status,created_at,completed_at from data_subject_request
                        where user_id=:userId and request_type='DELETION' order by created_at desc limit 1
                        """).param("userId", userId).query(this::mapRequest).optional();
    }

    private SubjectRequest mapRequest(java.sql.ResultSet rs, int row) throws java.sql.SQLException {
        return new SubjectRequest(rs.getObject("id", UUID.class), rs.getString("request_type"),
                rs.getString("status"), rs.getTimestamp("created_at").toInstant(),
                rs.getTimestamp("completed_at") == null ? null : rs.getTimestamp("completed_at").toInstant());
    }

    private static java.time.OffsetDateTime dbTime(Instant value) {
        return value == null ? null : java.time.OffsetDateTime.ofInstant(value, java.time.ZoneOffset.UTC);
    }
}
