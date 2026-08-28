package br.com.financetarget.subscriptions.infrastructure.payments;

import br.com.financetarget.config.FeatureFlagProperties;
import br.com.financetarget.subscriptions.application.PaymentsHub;
import br.com.financetarget.subscriptions.application.SubscriptionException;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.util.UUID;

@Component
public class MockPaymentsAdapter implements PaymentsHub {
    private final FeatureFlagProperties features;
    private final Clock clock;

    public MockPaymentsAdapter(FeatureFlagProperties features, Clock clock) {
        this.features = features;
        this.clock = clock;
    }

    @Override
    public CheckoutSession createCheckout(CheckoutRequest request) {
        if (!features.paymentsMock()) {
            throw new SubscriptionException(SubscriptionException.Kind.DISABLED, "PAYMENTS_MOCK_DISABLED",
                    "A simulação de assinatura não está habilitada neste ambiente.");
        }
        return new CheckoutSession("MOCK", "mock_" + UUID.randomUUID(), "SIMULATED", clock.instant());
    }
}
