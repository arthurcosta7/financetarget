package br.com.financetarget.subscriptions.infrastructure.payments;

import br.com.financetarget.config.FeatureFlagProperties;
import br.com.financetarget.config.MockIntegrationProperties;
import br.com.financetarget.subscriptions.application.PaymentsHub;
import br.com.financetarget.subscriptions.application.SubscriptionException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Clock;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.HexFormat;
import java.util.UUID;

@Component
public class MockPaymentWebhookVerifier {
    private final FeatureFlagProperties features;
    private final MockIntegrationProperties properties;
    private final ObjectMapper json;
    private final Clock clock;

    public MockPaymentWebhookVerifier(FeatureFlagProperties features, MockIntegrationProperties properties,
                                      ObjectMapper json, Clock clock) {
        this.features = features;
        this.properties = properties;
        this.json = json;
        this.clock = clock;
    }

    public VerifiedEvent verify(String eventId, String timestamp, String signature, byte[] payload) {
        requireEnabled();
        String normalizedEventId = normalize(eventId, 160, "INVALID_WEBHOOK_EVENT_ID");
        verifyTimestamp(timestamp);
        verifySignature(timestamp, signature, payload);
        MockWebhookPayload external;
        try {
            external = json.readValue(payload, MockWebhookPayload.class);
        } catch (Exception exception) {
            throw error(SubscriptionException.Kind.BAD_REQUEST, "INVALID_WEBHOOK_PAYLOAD",
                    "O evento simulado não possui um payload válido.");
        }
        if (external == null || external.userId() == null) {
            throw error(SubscriptionException.Kind.BAD_REQUEST, "INVALID_WEBHOOK_PAYLOAD",
                    "O evento simulado não possui todos os campos necessários.");
        }
        var canonical = new PaymentsHub.PaymentEvent(
                normalize(external.eventType(), 80, "INVALID_WEBHOOK_PAYLOAD"),
                external.userId(),
                normalize(external.planCode(), 64, "INVALID_WEBHOOK_PAYLOAD"),
                normalize(external.providerSubscriptionReference(), 160, "INVALID_WEBHOOK_PAYLOAD"));
        return new VerifiedEvent("MOCK", normalizedEventId, sha256(payload), canonical);
    }

    private void requireEnabled() {
        if (!features.paymentsMock()) {
            throw error(SubscriptionException.Kind.DISABLED, "PAYMENTS_MOCK_DISABLED",
                    "A simulação de assinatura não está habilitada neste ambiente.");
        }
    }

    private void verifyTimestamp(String value) {
        try {
            Instant supplied = Instant.parse(value);
            Instant now = clock.instant();
            if (supplied.isBefore(now.minus(properties.webhookReplayTolerance()))
                    || supplied.isAfter(now.plus(properties.webhookReplayTolerance()))) {
                throw error(SubscriptionException.Kind.UNAUTHORIZED, "WEBHOOK_REPLAY_WINDOW_EXCEEDED",
                        "O evento simulado está fora da janela permitida.");
            }
        } catch (DateTimeParseException | NullPointerException exception) {
            throw error(SubscriptionException.Kind.UNAUTHORIZED, "INVALID_WEBHOOK_TIMESTAMP",
                    "Timestamp do evento simulado inválido.");
        }
    }

    private void verifySignature(String timestamp, String supplied, byte[] payload) {
        String secret = properties.paymentWebhookSecret();
        if (secret == null || secret.length() < 24) {
            throw error(SubscriptionException.Kind.DISABLED, "MOCK_WEBHOOK_SECRET_NOT_CONFIGURED",
                    "O webhook simulado não está configurado com segurança.");
        }
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            mac.update(timestamp.getBytes(StandardCharsets.UTF_8));
            mac.update((byte) '.');
            String expected = HexFormat.of().formatHex(mac.doFinal(payload));
            if (supplied == null || !MessageDigest.isEqual(expected.getBytes(StandardCharsets.US_ASCII),
                    supplied.getBytes(StandardCharsets.US_ASCII))) {
                throw error(SubscriptionException.Kind.UNAUTHORIZED, "INVALID_WEBHOOK_SIGNATURE",
                        "Assinatura do evento simulado inválida.");
            }
        } catch (SubscriptionException exception) {
            throw exception;
        } catch (Exception exception) {
            throw error(SubscriptionException.Kind.DISABLED, "WEBHOOK_VERIFICATION_UNAVAILABLE",
                    "Não foi possível verificar o evento simulado.");
        }
    }

    private static String normalize(String value, int max, String code) {
        String normalized = value == null ? "" : value.trim();
        if (normalized.isEmpty() || normalized.length() > max) {
            throw error(SubscriptionException.Kind.BAD_REQUEST, code, "Informe um valor válido.");
        }
        return normalized;
    }

    private static String sha256(byte[] value) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(value));
        } catch (Exception exception) {
            throw new IllegalStateException(exception);
        }
    }

    private static SubscriptionException error(SubscriptionException.Kind kind, String code, String message) {
        return new SubscriptionException(kind, code, message);
    }

    private record MockWebhookPayload(String eventType, UUID userId, String planCode,
                                      String providerSubscriptionReference) {}
    public record VerifiedEvent(String provider, String eventId, String payloadHash,
                                PaymentsHub.PaymentEvent event) {}
}
