package br.com.financetarget.beta.application;

import br.com.financetarget.audit.application.AuditEventPort;
import br.com.financetarget.config.BetaProperties;
import br.com.financetarget.identity.application.IdentityException;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Set;
import java.util.UUID;

@Service
public class BetaLearningService {
    private static final Set<String> EVENTS = Set.of("DASHBOARD_VIEWED", "GOAL_CREATED", "SCENARIO_CREATED",
            "SPACE_CREATED", "INVITATION_ACCEPTED", "FEEDBACK_SUBMITTED");
    private static final Set<String> STAGES = Set.of("ACTIVATION", "PLANNING", "COLLABORATION", "RETENTION", "TRUST");
    private static final Set<String> OUTCOMES = Set.of("STARTED", "COMPLETED", "ABANDONED", "FAILED");
    private static final Set<String> DEVICES = Set.of("MOBILE", "TABLET", "DESKTOP", "UNKNOWN");
    private static final Set<String> CATEGORIES = Set.of("COMPREHENSION", "USABILITY", "TRUST", "COLLABORATION", "PROBLEM");

    private final JdbcClient jdbc;
    private final BetaProperties beta;
    private final AuditEventPort audit;
    private final Clock clock;
    private final MeterRegistry meters;

    public BetaLearningService(JdbcClient jdbc, BetaProperties beta, AuditEventPort audit, Clock clock,
                               MeterRegistry meters) {
        this.jdbc = jdbc;
        this.beta = beta;
        this.audit = audit;
        this.clock = clock;
        this.meters = meters;
    }

    public Config config() { return new Config(beta.enabled(), beta.enabled(), beta.maximumSharedMembers()); }

    @Transactional
    public void event(UUID userId, String eventName, String stage, String outcome, String deviceClass) {
        if (!beta.enabled()) return;
        String event = allowed(eventName, EVENTS, "INVALID_BETA_EVENT");
        String journey = allowed(stage, STAGES, "INVALID_BETA_STAGE");
        String result = allowed(outcome, OUTCOMES, "INVALID_BETA_OUTCOME");
        String device = allowed(deviceClass, DEVICES, "INVALID_DEVICE_CLASS");
        jdbc.sql("""
                insert into beta_product_event(id,user_id,event_name,journey_stage,outcome,device_class,occurred_at)
                values (:id,:userId,:event,:stage,:outcome,:device,:now)
                """).param("id", UUID.randomUUID()).param("userId", userId).param("event", event)
                .param("stage", journey).param("outcome", result).param("device", device)
                .param("now", dbTime(clock.instant())).update();
        meters.counter("financetarget.beta.product.events", "event", event, "outcome", result).increment();
    }

    @Transactional
    public Feedback feedback(UUID userId, String category, Integer rating, String comment) {
        if (!beta.enabled()) {
            throw new IdentityException(IdentityException.Kind.NOT_FOUND, "BETA_NOT_ENABLED", "O beta ainda não está habilitado.");
        }
        String normalizedCategory = allowed(category, CATEGORIES, "INVALID_FEEDBACK_CATEGORY");
        if (rating != null && (rating < 1 || rating > 5)) {
            throw bad("INVALID_FEEDBACK_RATING", "A avaliação deve estar entre 1 e 5.");
        }
        String normalizedComment = normalizeComment(comment);
        var id = UUID.randomUUID();
        var now = clock.instant();
        jdbc.sql("""
                insert into beta_feedback(id,user_id,category,rating,comment,status,created_at)
                values (:id,:userId,:category,:rating,:comment,'OPEN',:now)
                """).param("id", id).param("userId", userId).param("category", normalizedCategory)
                .param("rating", rating).param("comment", normalizedComment).param("now", dbTime(now)).update();
        audit.record(userId, "BETA_FEEDBACK_SUBMITTED", "BETA_FEEDBACK", id, "SUCCESS", now);
        meters.counter("financetarget.beta.feedback", "category", normalizedCategory).increment();
        return new Feedback(id, normalizedCategory, rating, "OPEN", now.toString());
    }

    private String normalizeComment(String value) {
        if (value == null || value.isBlank()) return null;
        String normalized = value.trim().replaceAll("\\s+", " ");
        if (normalized.length() > beta.feedbackCommentMaximumLength()) {
            throw bad("FEEDBACK_COMMENT_TOO_LONG", "O comentário excede o limite permitido.");
        }
        return normalized;
    }

    private static String allowed(String value, Set<String> allowed, String code) {
        String normalized = value == null ? "" : value.trim().toUpperCase(java.util.Locale.ROOT);
        if (!allowed.contains(normalized)) throw bad(code, "Valor não permitido para a telemetria do beta.");
        return normalized;
    }

    private static IdentityException bad(String code, String message) {
        return new IdentityException(IdentityException.Kind.BAD_REQUEST, code, message);
    }

    private static OffsetDateTime dbTime(java.time.Instant value) { return OffsetDateTime.ofInstant(value, ZoneOffset.UTC); }

    public record Config(boolean enabled, boolean feedbackEnabled, int maximumSharedMembers) {}
    public record Feedback(UUID id, String category, Integer rating, String status, String createdAt) {}
}
