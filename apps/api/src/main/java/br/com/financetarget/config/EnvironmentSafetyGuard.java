package br.com.financetarget.config;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.util.Set;

@Component
public class EnvironmentSafetyGuard {
    private static final Set<String> SUPPORTED = Set.of("dev", "test", "staging", "production");

    private final String environment;
    private final CorsProperties cors;
    private final AuthProperties auth;
    private final FeatureFlagProperties features;

    public EnvironmentSafetyGuard(@Value("${app.environment}") String environment, CorsProperties cors,
                                  AuthProperties auth, FeatureFlagProperties features) {
        this.environment = environment;
        this.cors = cors;
        this.auth = auth;
        this.features = features;
    }

    @PostConstruct
    void validate() {
        validate(environment, cors, auth, features);
    }

    static void validate(String environment, CorsProperties cors, AuthProperties auth,
                         FeatureFlagProperties features) {
        if (!SUPPORTED.contains(environment)) {
            throw new IllegalStateException("APP_ENV deve identificar um ambiente suportado.");
        }
        if (cors.allowedOrigins().isEmpty()) {
            throw new IllegalStateException("A allowlist CORS não pode estar vazia.");
        }
        if (auth.minimumPasswordLength() < 15 || auth.maximumPasswordLength() < auth.minimumPasswordLength()) {
            throw new IllegalStateException("A política de senha configurada é insegura.");
        }
        if (!Set.of("staging", "production").contains(environment)) return;
        if (!auth.secureCookies()) {
            throw new IllegalStateException("Cookies seguros são obrigatórios fora de dev e test.");
        }
        if (cors.allowedOrigins().stream().anyMatch(origin -> !isPublicHttps(origin))) {
            throw new IllegalStateException("Staging e produção exigem origens CORS HTTPS não locais.");
        }
        if (features.paymentsMock() || features.notificationsMock() || features.openFinance()
                || features.loyalty() || features.travel() || features.realEstateFinancing()
                || features.autoFinancing()) {
            throw new IllegalStateException("Integrações não aprovadas devem permanecer desligadas neste ambiente.");
        }
    }

    private static boolean isPublicHttps(String origin) {
        try {
            URI uri = URI.create(origin);
            String host = uri.getHost();
            return "https".equalsIgnoreCase(uri.getScheme()) && host != null
                    && !host.equalsIgnoreCase("localhost") && !host.equals("127.0.0.1")
                    && !host.equals("::1") && uri.getUserInfo() == null && uri.getPath().isEmpty();
        } catch (IllegalArgumentException exception) {
            return false;
        }
    }
}
