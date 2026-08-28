package br.com.financetarget.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties("app.mock-integrations")
public record MockIntegrationProperties(String paymentWebhookSecret, Duration webhookReplayTolerance) {
    public MockIntegrationProperties {
        if (webhookReplayTolerance == null || webhookReplayTolerance.isNegative()
                || webhookReplayTolerance.isZero()) {
            throw new IllegalArgumentException("app.mock-integrations.webhook-replay-tolerance deve ser positiva");
        }
    }
}
