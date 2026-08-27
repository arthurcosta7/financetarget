package br.com.financetarget.audit.infrastructure.persistence;

import br.com.financetarget.audit.application.AuditEventPort;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

@Repository
public class JdbcAuditEventAdapter implements AuditEventPort {
    private final JdbcClient jdbc;

    public JdbcAuditEventAdapter(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public void record(UUID actorUserId, String action, String resourceType, UUID resourceId,
                       String outcome, Instant occurredAt) {
        jdbc.sql("""
                        insert into audit_event(id,actor_user_id,action,resource_type,resource_id,outcome,occurred_at)
                        values (:id,:actor,:action,:resourceType,:resourceId,:outcome,:occurredAt)
                        """).param("id", UUID.randomUUID()).param("actor", actorUserId).param("action", action)
                .param("resourceType", resourceType).param("resourceId", resourceId).param("outcome", outcome)
                .param("occurredAt", OffsetDateTime.ofInstant(occurredAt, ZoneOffset.UTC)).update();
    }
}
