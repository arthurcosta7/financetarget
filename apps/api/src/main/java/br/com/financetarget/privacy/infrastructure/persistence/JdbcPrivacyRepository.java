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
        return new ExportData(account, profile, consents);
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
