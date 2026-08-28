package br.com.financetarget.subscriptions.application;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public interface SubscriptionRepository {
    record Plan(String code, String displayName, Map<String, String> entitlements) {}
    record Subscription(UUID id, UUID userId, String planCode, String status, String provider,
                        String providerReference, long version, Instant updatedAt) {}
    record Checkout(UUID id, UUID userId, String planCode, String provider, String providerReference,
                    String idempotencyKey, String requestHash, String status, Instant createdAt) {}
    record WebhookEvent(String provider, String eventId, String eventType, String payloadHash, String status) {}

    List<Plan> listActivePlans();
    Optional<Plan> findActivePlan(String code);
    Optional<Subscription> findSubscription(UUID userId);
    Optional<Checkout> findCheckout(UUID userId, String idempotencyKey);
    Checkout insertCheckout(UUID userId, String planCode, PaymentsHub.CheckoutSession session,
                            String idempotencyKey, String requestHash);
    Optional<WebhookEvent> findWebhook(String provider, String eventId);
    boolean claimWebhook(String provider, String eventId, String eventType, String payloadHash, Instant receivedAt);
    Subscription applySubscription(UUID userId, String planCode, String status, String provider,
                                   String providerReference, Instant updatedAt);
    void completeWebhook(String provider, String eventId, Instant processedAt);
}
