package br.com.financetarget.subscriptions.application;

import br.com.financetarget.audit.application.AuditEventPort;
import br.com.financetarget.config.FeatureFlagProperties;
import br.com.financetarget.notifications.application.NotificationHub;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Clock;
import java.time.Instant;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class SubscriptionService {
    private final SubscriptionRepository repository;
    private final PaymentsHub payments;
    private final NotificationHub notifications;
    private final AuditEventPort audit;
    private final FeatureFlagProperties features;
    private final Clock clock;

    public SubscriptionService(SubscriptionRepository repository, PaymentsHub payments,
                               NotificationHub notifications, AuditEventPort audit,
                               FeatureFlagProperties features, Clock clock) {
        this.repository = repository;
        this.payments = payments;
        this.notifications = notifications;
        this.audit = audit;
        this.features = features;
        this.clock = clock;
    }

    public Overview overview(UUID userId) {
        List<SubscriptionRepository.Plan> plans = repository.listActivePlans();
        Optional<SubscriptionRepository.Subscription> subscription = repository.findSubscription(userId);
        Map<String, String> entitlements = subscription.flatMap(value -> repository.findActivePlan(value.planCode()))
                .map(SubscriptionRepository.Plan::entitlements).orElse(Map.of());
        return new Overview(subscription, entitlements, plans, features.paymentsMock());
    }

    @Transactional
    public CheckoutView createCheckout(UUID userId, String planCode, String idempotencyKey) {
        requireMockEnabled();
        String normalizedPlan = normalize(planCode, 64, "INVALID_PLAN_CODE");
        String normalizedKey = normalize(idempotencyKey, 128, "INVALID_IDEMPOTENCY_KEY");
        String requestHash = sha256(normalizedPlan);
        var existing = repository.findCheckout(userId, normalizedKey);
        if (existing.isPresent()) {
            if (!existing.get().requestHash().equals(requestHash)) {
                throw error(SubscriptionException.Kind.CONFLICT, "IDEMPOTENCY_KEY_REUSED",
                        "Esta chave já foi usada com outro plano.");
            }
            return CheckoutView.from(existing.get());
        }
        repository.findActivePlan(normalizedPlan).orElseThrow(() -> error(SubscriptionException.Kind.NOT_FOUND,
                "PLAN_NOT_FOUND", "Plano não encontrado neste ambiente."));
        var session = payments.createCheckout(new PaymentsHub.CheckoutRequest(userId, normalizedPlan, normalizedKey));
        var stored = repository.insertCheckout(userId, normalizedPlan, session, normalizedKey, requestHash);
        if (!stored.requestHash().equals(requestHash)) {
            throw error(SubscriptionException.Kind.CONFLICT, "IDEMPOTENCY_KEY_REUSED",
                    "Esta chave já foi usada com outro plano.");
        }
        audit.record(userId, "MOCK_CHECKOUT_CREATED", "CHECKOUT_SESSION", stored.id(), "SUCCESS", clock.instant());
        return CheckoutView.from(stored);
    }

    @Transactional
    public Overview processPaymentEvent(String provider, String eventId, String payloadHash,
                                        PaymentsHub.PaymentEvent event) {
        var existing = repository.findWebhook(provider, eventId);
        if (existing.isPresent()) {
            ensureSamePayload(existing.get(), payloadHash);
            return overview(event.accountId());
        }
        boolean claimed = repository.claimWebhook(provider, eventId, event.type(), payloadHash, clock.instant());
        if (!claimed) {
            var concurrent = repository.findWebhook(provider, eventId).orElseThrow(() -> error(
                    SubscriptionException.Kind.CONFLICT, "WEBHOOK_ALREADY_PROCESSING",
                    "O evento já está em processamento."));
            ensureSamePayload(concurrent, payloadHash);
            return overview(event.accountId());
        }
        repository.findActivePlan(event.planCode()).orElseThrow(() -> error(SubscriptionException.Kind.NOT_FOUND,
                "PLAN_NOT_FOUND", "Plano não encontrado neste ambiente."));
        String status = switch (event.type()) {
            case "subscription.activated" -> "ACTIVE";
            case "subscription.past_due" -> "PAST_DUE";
            case "subscription.canceled" -> "CANCELED";
            default -> throw error(SubscriptionException.Kind.BAD_REQUEST, "UNSUPPORTED_WEBHOOK_EVENT",
                    "Tipo de evento simulado não suportado.");
        };
        var now = clock.instant();
        var subscription = repository.applySubscription(event.accountId(), event.planCode(), status, provider,
                event.subscriptionReference(), now);
        repository.completeWebhook(provider, eventId, now);
        notifications.deliver(new NotificationHub.Message(event.accountId(), "ESSENTIAL",
                "SUBSCRIPTION_STATUS_CHANGED", "EMAIL", now));
        audit.record(null, "PAYMENT_WEBHOOK_PROCESSED", "SUBSCRIPTION", subscription.id(), "SUCCESS", now);
        return overview(event.accountId());
    }

    private static void ensureSamePayload(SubscriptionRepository.WebhookEvent existing, String payloadHash) {
        if (!MessageDigest.isEqual(existing.payloadHash().getBytes(StandardCharsets.US_ASCII),
                payloadHash.getBytes(StandardCharsets.US_ASCII))) {
            throw error(SubscriptionException.Kind.CONFLICT, "WEBHOOK_EVENT_REUSED",
                    "O identificador do evento já foi usado com outro conteúdo.");
        }
    }

    private void requireMockEnabled() {
        if (!features.paymentsMock()) {
            throw error(SubscriptionException.Kind.DISABLED, "PAYMENTS_MOCK_DISABLED",
                    "A simulação de assinatura não está habilitada neste ambiente.");
        }
    }

    private static String normalize(String value, int max, String code) {
        String normalized = value == null ? "" : value.trim();
        if (normalized.isEmpty() || normalized.length() > max) {
            throw error(SubscriptionException.Kind.BAD_REQUEST, code, "Informe um valor válido.");
        }
        return normalized;
    }

    private static String sha256(String value) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception exception) {
            throw new IllegalStateException(exception);
        }
    }

    private static SubscriptionException error(SubscriptionException.Kind kind, String code, String message) {
        return new SubscriptionException(kind, code, message);
    }

    public record Overview(Optional<SubscriptionRepository.Subscription> subscription,
                           Map<String, String> entitlements,
                           List<SubscriptionRepository.Plan> availablePlans,
                           boolean mockCheckoutEnabled) {}
    public record CheckoutView(UUID id, String planCode, String provider, String reference,
                               String status, Instant createdAt) {
        static CheckoutView from(SubscriptionRepository.Checkout value) {
            return new CheckoutView(value.id(), value.planCode(), value.provider(), value.providerReference(),
                    value.status(), value.createdAt());
        }
    }
}
