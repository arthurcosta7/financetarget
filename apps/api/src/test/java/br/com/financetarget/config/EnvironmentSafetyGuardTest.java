package br.com.financetarget.config;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.net.URI;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatIllegalStateException;
import static org.assertj.core.api.Assertions.assertThatNoException;

class EnvironmentSafetyGuardTest {
    private static final FeatureFlagProperties DISABLED = new FeatureFlagProperties(
            false, false, false, false, false, false, false);

    @Test
    void acceptsAClosedStagingConfiguration() {
        assertThatNoException().isThrownBy(() -> EnvironmentSafetyGuard.validate("staging",
                new CorsProperties(List.of("https://staging.example.test")), auth(true), DISABLED, resend(false), beta(false),
                legal("staging-review-v1", "staging-review-v1"), deployment("local"), "local"));
    }

    @Test
    void rejectsInsecureCookiesLocalOriginsAndIntegrationFlagsInStaging() {
        assertThatIllegalStateException().isThrownBy(() -> EnvironmentSafetyGuard.validate("staging",
                new CorsProperties(List.of("https://staging.example.test")), auth(false), DISABLED, resend(false), beta(false),
                legal("staging-review-v1", "staging-review-v1"), deployment("local"), "local"));
        assertThatIllegalStateException().isThrownBy(() -> EnvironmentSafetyGuard.validate("staging",
                new CorsProperties(List.of("http://localhost:3000")), auth(true), DISABLED, resend(false), beta(false),
                legal("staging-review-v1", "staging-review-v1"), deployment("local"), "local"));
        var enabled = new FeatureFlagProperties(true, false, false, false, false, false, false);
        assertThatIllegalStateException().isThrownBy(() -> EnvironmentSafetyGuard.validate("staging",
                new CorsProperties(List.of("https://staging.example.test")), auth(true), enabled, resend(false), beta(false),
                legal("staging-review-v1", "staging-review-v1"), deployment("local"), "local"));
    }

    @Test
    void requiresSafeResendConfigurationWhenEnabled() {
        assertThatNoException().isThrownBy(() -> EnvironmentSafetyGuard.validate("staging",
                new CorsProperties(List.of("https://staging.example.test")), auth(true), DISABLED, resend(true), beta(false),
                legal("staging-review-v1", "staging-review-v1"), deployment("local"), "local"));
        var invalid = new ResendProperties(true, "", URI.create("http://api.resend.test/emails"),
                "", "FinanceTarget", URI.create("http://localhost:3000"), Duration.ofSeconds(3), Duration.ofSeconds(8));
        assertThatIllegalStateException().isThrownBy(() -> EnvironmentSafetyGuard.validate("staging",
                new CorsProperties(List.of("https://staging.example.test")), auth(true), DISABLED, invalid, beta(false),
                legal("staging-review-v1", "staging-review-v1"), deployment("local"), "local"));
    }

    @Test
    void rejectsBetaEnablementOutsideDevAndTestUntilTheHumanGateIsApproved() {
        assertThatIllegalStateException().isThrownBy(() -> EnvironmentSafetyGuard.validate("staging",
                new CorsProperties(List.of("https://staging.example.test")), auth(true), DISABLED,
                resend(false), beta(true), legal("staging-review-v1", "staging-review-v1"), deployment("local"), "local"));
    }

    @Test
    void acceptsProductionOnlyWithTraceableReleaseApprovedDocumentsAndIdentityDelivery() {
        assertThatNoException().isThrownBy(() -> EnvironmentSafetyGuard.validate("production",
                new CorsProperties(List.of("https://app.example.test")), auth(true), DISABLED,
                resend(true), beta(false), legal("terms-v1", "privacy-v1"),
                deployment("0123456789abcdef0123456789abcdef01234567"),
                "0123456789abcdef0123456789abcdef01234567"));
    }

    @Test
    void rejectsUntraceableOrOperationallyIncompleteProductionConfiguration() {
        var cors = new CorsProperties(List.of("https://app.example.test"));
        assertThatIllegalStateException().isThrownBy(() -> EnvironmentSafetyGuard.validate("production",
                cors, auth(true), DISABLED, resend(true), beta(false), legal("terms-v1", "privacy-v1"),
                deployment("latest"), "0123456789abcdef0123456789abcdef01234567"));
        assertThatIllegalStateException().isThrownBy(() -> EnvironmentSafetyGuard.validate("production",
                cors, auth(true), DISABLED, resend(true), beta(false), legal("draft-v1", "privacy-v1"),
                deployment("0123456789abcdef0123456789abcdef01234567"),
                "0123456789abcdef0123456789abcdef01234567"));
        assertThatIllegalStateException().isThrownBy(() -> EnvironmentSafetyGuard.validate("production",
                cors, auth(true), DISABLED, resend(false), beta(false), legal("terms-v1", "privacy-v1"),
                deployment("0123456789abcdef0123456789abcdef01234567"),
                "0123456789abcdef0123456789abcdef01234567"));
        assertThatIllegalStateException().isThrownBy(() -> EnvironmentSafetyGuard.validate("production",
                cors, auth(true), DISABLED, resend(true), beta(false), legal("terms-v1", "privacy-v1"),
                deployment("0123456789abcdef0123456789abcdef01234567"),
                "fedcba9876543210fedcba9876543210fedcba98"));
    }

    private static AuthProperties auth(boolean secure) {
        return new AuthProperties(Duration.ofMinutes(15), Duration.ofDays(30), Duration.ofHours(24),
                Duration.ofMinutes(30), secure, "ft_access", "ft_refresh", 15, 128, 10,
                Duration.ofMinutes(1));
    }

    private static ResendProperties resend(boolean enabled) {
        return new ResendProperties(enabled, "re_synthetic_only", URI.create("https://api.resend.test/emails"),
                "noreply@example.test", "FinanceTarget", URI.create("https://staging.example.test"),
                Duration.ofSeconds(3), Duration.ofSeconds(8));
    }

    private static BetaProperties beta(boolean enabled) {
        return new BetaProperties(enabled, Duration.ofDays(7), 5, 2, 500);
    }

    private static LegalDocumentProperties legal(String terms, String privacy) {
        return new LegalDocumentProperties(terms, privacy);
    }

    private static DeploymentProperties deployment(String releaseId) {
        return new DeploymentProperties(releaseId);
    }
}
