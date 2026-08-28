package br.com.financetarget.config;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatIllegalStateException;
import static org.assertj.core.api.Assertions.assertThatNoException;

class EnvironmentSafetyGuardTest {
    private static final FeatureFlagProperties DISABLED = new FeatureFlagProperties(
            false, false, false, false, false, false, false);

    @Test
    void acceptsAClosedStagingConfiguration() {
        assertThatNoException().isThrownBy(() -> EnvironmentSafetyGuard.validate("staging",
                new CorsProperties(List.of("https://staging.example.test")), auth(true), DISABLED));
    }

    @Test
    void rejectsInsecureCookiesLocalOriginsAndIntegrationFlagsInStaging() {
        assertThatIllegalStateException().isThrownBy(() -> EnvironmentSafetyGuard.validate("staging",
                new CorsProperties(List.of("https://staging.example.test")), auth(false), DISABLED));
        assertThatIllegalStateException().isThrownBy(() -> EnvironmentSafetyGuard.validate("staging",
                new CorsProperties(List.of("http://localhost:3000")), auth(true), DISABLED));
        var enabled = new FeatureFlagProperties(true, false, false, false, false, false, false);
        assertThatIllegalStateException().isThrownBy(() -> EnvironmentSafetyGuard.validate("staging",
                new CorsProperties(List.of("https://staging.example.test")), auth(true), enabled));
    }

    private static AuthProperties auth(boolean secure) {
        return new AuthProperties(Duration.ofMinutes(15), Duration.ofDays(30), Duration.ofHours(24),
                Duration.ofMinutes(30), secure, "ft_access", "ft_refresh", 15, 128, 10,
                Duration.ofMinutes(1));
    }
}
