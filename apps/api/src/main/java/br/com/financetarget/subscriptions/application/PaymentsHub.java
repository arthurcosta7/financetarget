package br.com.financetarget.subscriptions.application;

import java.time.Instant;
import java.util.UUID;

public interface PaymentsHub {
    record CheckoutRequest(UUID accountId, String planCode, String idempotencyKey) {}
    record CheckoutSession(String provider, String reference, String status, Instant createdAt) {}
    record PaymentEvent(String type, UUID accountId, String planCode, String subscriptionReference) {}

    CheckoutSession createCheckout(CheckoutRequest request);
}
