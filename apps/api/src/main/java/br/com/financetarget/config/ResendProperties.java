package br.com.financetarget.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.net.URI;
import java.time.Duration;

@ConfigurationProperties("app.integrations.resend")
public record ResendProperties(
        boolean enabled,
        String apiKey,
        URI endpoint,
        String fromEmail,
        String fromName,
        URI frontendBaseUrl,
        Duration connectTimeout,
        Duration readTimeout
) {
}
