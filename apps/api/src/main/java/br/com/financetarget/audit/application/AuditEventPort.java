package br.com.financetarget.audit.application;

import java.time.Instant;
import java.util.UUID;

public interface AuditEventPort {
    void record(UUID actorUserId, String action, String resourceType, UUID resourceId, String outcome, Instant occurredAt);
}
