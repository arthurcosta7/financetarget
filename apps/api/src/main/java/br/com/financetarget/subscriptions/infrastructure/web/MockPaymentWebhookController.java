package br.com.financetarget.subscriptions.infrastructure.web;

import br.com.financetarget.subscriptions.application.SubscriptionService;
import br.com.financetarget.subscriptions.infrastructure.payments.MockPaymentWebhookVerifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/integrations/payments/webhooks/mock")
public class MockPaymentWebhookController {
    private final SubscriptionService subscriptions;
    private final MockPaymentWebhookVerifier verifier;

    public MockPaymentWebhookController(SubscriptionService subscriptions, MockPaymentWebhookVerifier verifier) {
        this.subscriptions = subscriptions;
        this.verifier = verifier;
    }

    @PostMapping
    ResponseEntity<Void> receive(@RequestHeader("X-Mock-Event-Id") String eventId,
                                 @RequestHeader("X-Mock-Timestamp") String timestamp,
                                 @RequestHeader("X-Mock-Signature") String signature,
                                 @RequestBody byte[] payload) {
        var verified = verifier.verify(eventId, timestamp, signature, payload);
        subscriptions.processPaymentEvent(verified.provider(), verified.eventId(), verified.payloadHash(),
                verified.event());
        return ResponseEntity.accepted().build();
    }
}
