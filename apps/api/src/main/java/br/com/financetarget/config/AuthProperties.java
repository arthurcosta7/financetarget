package br.com.financetarget.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties("app.auth")
public record AuthProperties(
        Duration accessTtl,
        Duration refreshTtl,
        Duration verificationTtl,
        Duration recoveryTtl,
        boolean secureCookies,
        String accessCookieName,
        String refreshCookieName,
        int minimumPasswordLength,
        int maximumPasswordLength,
        int attemptLimit,
        Duration attemptWindow
) {
}
