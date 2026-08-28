package br.com.financetarget.subscriptions.infrastructure.persistence;

import br.com.financetarget.subscriptions.application.PaymentsHub;
import br.com.financetarget.subscriptions.application.SubscriptionRepository;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Repository
public class JdbcSubscriptionRepository implements SubscriptionRepository {
    private final JdbcClient jdbc;

    public JdbcSubscriptionRepository(JdbcClient jdbc) { this.jdbc = jdbc; }

    @Override
    public List<Plan> listActivePlans() {
        return jdbc.sql("select code,display_name from subscription_plan where status='ACTIVE' order by code")
                .query((rs, row) -> plan(rs.getString("code"), rs.getString("display_name"))).list();
    }

    @Override
    public Optional<Plan> findActivePlan(String code) {
        return jdbc.sql("select code,display_name from subscription_plan where code=:code and status='ACTIVE'")
                .param("code", code).query((rs, row) -> plan(rs.getString("code"), rs.getString("display_name")))
                .optional();
    }

    private Plan plan(String code, String displayName) {
        Map<String, String> entitlements = new LinkedHashMap<>();
        jdbc.sql("""
                        select entitlement_key,entitlement_value from plan_entitlement
                        where plan_code=:code order by entitlement_key
                        """).param("code", code).query((rs, row) -> Map.entry(
                        rs.getString("entitlement_key"), rs.getString("entitlement_value")))
                .list().forEach(item -> entitlements.put(item.getKey(), item.getValue()));
        return new Plan(code, displayName, Map.copyOf(entitlements));
    }

    @Override
    public Optional<Subscription> findSubscription(UUID userId) {
        return jdbc.sql("select * from account_subscription where user_id=:userId")
                .param("userId", userId).query(this::mapSubscription).optional();
    }

    @Override
    public Optional<Checkout> findCheckout(UUID userId, String idempotencyKey) {
        return jdbc.sql("select * from checkout_session where user_id=:userId and idempotency_key=:key")
                .param("userId", userId).param("key", idempotencyKey).query(this::mapCheckout).optional();
    }

    @Override
    public Checkout insertCheckout(UUID userId, String planCode, PaymentsHub.CheckoutSession session,
                                   String idempotencyKey, String requestHash) {
        UUID id = UUID.randomUUID();
        jdbc.sql("""
                        insert into checkout_session(id,user_id,plan_code,provider,provider_reference,
                            idempotency_key,request_hash,status,created_at)
                        values (:id,:userId,:planCode,:provider,:reference,:key,:hash,:status,:createdAt)
                        on conflict (user_id,idempotency_key) do nothing
                        """).param("id", id).param("userId", userId).param("planCode", planCode)
                .param("provider", session.provider()).param("reference", session.reference())
                .param("key", idempotencyKey).param("hash", requestHash).param("status", session.status())
                .param("createdAt", dbTime(session.createdAt())).update();
        return findCheckout(userId, idempotencyKey).orElseThrow();
    }

    @Override
    public Optional<WebhookEvent> findWebhook(String provider, String eventId) {
        return jdbc.sql("select * from payment_webhook_event where provider=:provider and event_id=:eventId")
                .param("provider", provider).param("eventId", eventId).query((rs, row) -> new WebhookEvent(
                        rs.getString("provider"), rs.getString("event_id"), rs.getString("event_type"),
                        rs.getString("payload_hash"), rs.getString("status"))).optional();
    }

    @Override
    public boolean claimWebhook(String provider, String eventId, String eventType, String payloadHash,
                                Instant receivedAt) {
        return jdbc.sql("""
                        insert into payment_webhook_event(id,provider,event_id,event_type,payload_hash,status,received_at)
                        values (:id,:provider,:eventId,:eventType,:payloadHash,'RECEIVED',:receivedAt)
                        on conflict (provider,event_id) do nothing
                        """).param("id", UUID.randomUUID()).param("provider", provider).param("eventId", eventId)
                .param("eventType", eventType).param("payloadHash", payloadHash)
                .param("receivedAt", dbTime(receivedAt)).update() == 1;
    }

    @Override
    public Subscription applySubscription(UUID userId, String planCode, String status, String provider,
                                          String providerReference, Instant updatedAt) {
        jdbc.sql("""
                        insert into account_subscription(id,user_id,plan_code,status,provider,
                            provider_subscription_ref,version,updated_at)
                        values (:id,:userId,:planCode,:status,:provider,:providerReference,0,:updatedAt)
                        on conflict (user_id) do update set plan_code=excluded.plan_code,status=excluded.status,
                            provider=excluded.provider,provider_subscription_ref=excluded.provider_subscription_ref,
                            version=account_subscription.version+1,updated_at=excluded.updated_at
                        """).param("id", UUID.randomUUID()).param("userId", userId).param("planCode", planCode)
                .param("status", status).param("provider", provider).param("providerReference", providerReference)
                .param("updatedAt", dbTime(updatedAt)).update();
        return findSubscription(userId).orElseThrow();
    }

    @Override
    public void completeWebhook(String provider, String eventId, Instant processedAt) {
        jdbc.sql("""
                        update payment_webhook_event set status='PROCESSED',processed_at=:processedAt
                        where provider=:provider and event_id=:eventId
                        """).param("processedAt", dbTime(processedAt)).param("provider", provider)
                .param("eventId", eventId).update();
    }

    private Subscription mapSubscription(ResultSet rs, int row) throws SQLException {
        return new Subscription(rs.getObject("id", UUID.class), rs.getObject("user_id", UUID.class),
                rs.getString("plan_code"), rs.getString("status"), rs.getString("provider"),
                rs.getString("provider_subscription_ref"), rs.getLong("version"),
                rs.getTimestamp("updated_at").toInstant());
    }

    private Checkout mapCheckout(ResultSet rs, int row) throws SQLException {
        return new Checkout(rs.getObject("id", UUID.class), rs.getObject("user_id", UUID.class),
                rs.getString("plan_code"), rs.getString("provider"), rs.getString("provider_reference"),
                rs.getString("idempotency_key"), rs.getString("request_hash"), rs.getString("status"),
                rs.getTimestamp("created_at").toInstant());
    }

    private static OffsetDateTime dbTime(Instant value) {
        return OffsetDateTime.ofInstant(value, ZoneOffset.UTC);
    }
}
