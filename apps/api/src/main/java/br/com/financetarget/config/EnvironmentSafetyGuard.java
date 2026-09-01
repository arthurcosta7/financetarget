package br.com.financetarget.config;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.info.BuildProperties;
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
    private final ResendProperties resend;
    private final BetaProperties beta;
    private final LegalDocumentProperties legalDocuments;
    private final DeploymentProperties deployment;
    private final BuildProperties buildProperties;

    public EnvironmentSafetyGuard(@Value("${app.environment}") String environment, CorsProperties cors,
                                  AuthProperties auth, FeatureFlagProperties features, ResendProperties resend,
                                  BetaProperties beta, LegalDocumentProperties legalDocuments,
                                  DeploymentProperties deployment, BuildProperties buildProperties) {
        this.environment = environment;
        this.cors = cors;
        this.auth = auth;
        this.features = features;
        this.resend = resend;
        this.beta = beta;
        this.legalDocuments = legalDocuments;
        this.deployment = deployment;
        this.buildProperties = buildProperties;
    }

    @PostConstruct
    void validate() {
        validate(environment, cors, auth, features, resend, beta, legalDocuments, deployment,
                buildProperties.get("revision"));
    }

    static void validate(String environment, CorsProperties cors, AuthProperties auth,
                         FeatureFlagProperties features, ResendProperties resend, BetaProperties beta,
                         LegalDocumentProperties legalDocuments, DeploymentProperties deployment,
                         String builtReleaseId) {
        if (!SUPPORTED.contains(environment)) {
            throw new IllegalStateException("APP_ENV deve identificar um ambiente suportado.");
        }
        if (cors.allowedOrigins().isEmpty()) {
            throw new IllegalStateException("A allowlist CORS não pode estar vazia.");
        }
        if (auth.minimumPasswordLength() < 15 || auth.maximumPasswordLength() < auth.minimumPasswordLength()) {
            throw new IllegalStateException("A política de senha configurada é insegura.");
        }
        validateResend(environment, resend);
        if (!Set.of("staging", "production").contains(environment)) return;
        if (beta.enabled()) {
            throw new IllegalStateException("O beta exige aprovação manual antes de ser habilitado fora de dev e test.");
        }
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
        if ("production".equals(environment)) {
            validateProduction(resend, legalDocuments, deployment, builtReleaseId);
        }
    }

    private static void validateProduction(ResendProperties resend, LegalDocumentProperties legalDocuments,
                                           DeploymentProperties deployment, String builtReleaseId) {
        if (deployment.expectedReleaseId() == null
                || !deployment.expectedReleaseId().matches("^[a-f0-9]{40}$")) {
            throw new IllegalStateException("Produção exige APP_EXPECTED_RELEASE_ID com o SHA completo aprovado.");
        }
        if (!deployment.expectedReleaseId().equals(builtReleaseId)) {
            throw new IllegalStateException("O artefato iniciado não corresponde ao release aprovado.");
        }
        if (isProvisionalLegalVersion(legalDocuments.termsVersion())
                || isProvisionalLegalVersion(legalDocuments.privacyNoticeVersion())) {
            throw new IllegalStateException("Produção exige versões jurídicas aprovadas e não provisórias.");
        }
        if (!resend.enabled()) {
            throw new IllegalStateException("Produção exige entrega transacional de identidade habilitada.");
        }
    }

    private static boolean isProvisionalLegalVersion(String value) {
        if (value == null || value.isBlank()) return true;
        String normalized = value.toLowerCase(java.util.Locale.ROOT);
        return normalized.contains("draft") || normalized.contains("review")
                || normalized.contains("test") || normalized.contains("local");
    }

    private static void validateResend(String environment, ResendProperties resend) {
        if (!resend.enabled()) return;
        if (resend.apiKey() == null || !resend.apiKey().startsWith("re_") || resend.apiKey().length() < 12) {
            throw new IllegalStateException("A chave do Resend está ausente ou inválida.");
        }
        if (containsLineBreak(resend.fromEmail()) || resend.fromEmail() == null
                || !resend.fromEmail().contains("@")) {
            throw new IllegalStateException("O remetente do Resend está ausente ou inválido.");
        }
        if (resend.fromName() == null || resend.fromName().isBlank() || containsLineBreak(resend.fromName())) {
            throw new IllegalStateException("O nome do remetente do Resend está ausente ou inválido.");
        }
        if (resend.endpoint() == null || !"https".equalsIgnoreCase(resend.endpoint().getScheme())) {
            throw new IllegalStateException("O endpoint do Resend deve usar HTTPS.");
        }
        if (resend.frontendBaseUrl() == null || resend.frontendBaseUrl().getHost() == null
                || (Set.of("staging", "production").contains(environment)
                && !isPublicHttps(resend.frontendBaseUrl().toString()))) {
            throw new IllegalStateException("A URL pública usada nos e-mails é inválida para o ambiente.");
        }
        if (resend.connectTimeout() == null || resend.connectTimeout().isNegative()
                || resend.connectTimeout().isZero() || resend.readTimeout() == null
                || resend.readTimeout().isNegative() || resend.readTimeout().isZero()) {
            throw new IllegalStateException("Os timeouts do Resend devem ser positivos.");
        }
    }

    private static boolean containsLineBreak(String value) {
        return value != null && (value.contains("\r") || value.contains("\n"));
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
