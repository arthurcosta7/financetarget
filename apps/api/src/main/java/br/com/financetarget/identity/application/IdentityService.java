package br.com.financetarget.identity.application;

import br.com.financetarget.config.AuthProperties;
import br.com.financetarget.audit.application.AuditEventPort;
import br.com.financetarget.planningspace.application.PersonalSpaceProvisioner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.Locale;
import java.util.UUID;

@Service
public class IdentityService {
    public static final String GENERIC_REGISTRATION_MESSAGE =
            "Se o endereço puder ser cadastrado, enviaremos as próximas instruções.";
    public static final String GENERIC_RECOVERY_MESSAGE =
            "Se existir uma conta elegível, enviaremos as próximas instruções.";
    public static final String GENERIC_VERIFICATION_MESSAGE =
            "Se existir uma conta pendente, enviaremos uma nova mensagem de verificação.";

    private final IdentityRepository repository;
    private final IdentityMessagePort messages;
    private final TokenService tokens;
    private final PasswordEncoder passwordEncoder;
    private final AuthProperties properties;
    private final Clock clock;
    private final AuthenticationAttemptLimiter attemptLimiter;
    private final AuditEventPort audit;
    private final PersonalSpaceProvisioner personalSpaces;
    private final String dummyPasswordHash;

    public IdentityService(IdentityRepository repository, IdentityMessagePort messages, TokenService tokens,
                           PasswordEncoder passwordEncoder, AuthProperties properties, Clock clock,
                           AuthenticationAttemptLimiter attemptLimiter, AuditEventPort audit,
                           PersonalSpaceProvisioner personalSpaces) {
        this.repository = repository;
        this.messages = messages;
        this.tokens = tokens;
        this.passwordEncoder = passwordEncoder;
        this.properties = properties;
        this.clock = clock;
        this.attemptLimiter = attemptLimiter;
        this.audit = audit;
        this.personalSpaces = personalSpaces;
        this.dummyPasswordHash = passwordEncoder.encode("timing-sentinel-value-not-used");
    }

    @Transactional
    public String register(String email, String displayName, String password) {
        String normalizedEmail = normalizeEmail(email);
        attemptLimiter.check("register", normalizedEmail);
        validateName(displayName);
        validatePassword(password);
        String encodedPassword = passwordEncoder.encode(password);
        if (repository.emailExists(normalizedEmail)) {
            return GENERIC_REGISTRATION_MESSAGE;
        }

        Instant now = clock.instant();
        UUID userId = UUID.randomUUID();
        String rawToken = tokens.generate();
        boolean created = repository.createPendingAccount(userId, normalizedEmail, displayName.trim(), encodedPassword,
                UUID.randomUUID(), tokens.hash(rawToken), now.plus(properties.verificationTtl()), now);
        if (!created) return GENERIC_REGISTRATION_MESSAGE;
        audit.record(userId, "ACCOUNT_REGISTERED", "USER", userId, "SUCCESS", now);
        messages.sendVerification(normalizedEmail, rawToken);
        return GENERIC_REGISTRATION_MESSAGE;
    }

    @Transactional
    public void verifyEmail(String rawToken) {
        Instant now = clock.instant();
        IdentityRepository.IdentityToken token = validIdentityToken(rawToken, "VERIFY_EMAIL", now);
        repository.activateVerifiedAccount(token, now);
        personalSpaces.createForVerifiedUser(token.userId(), now);
        audit.record(token.userId(), "EMAIL_VERIFIED", "USER", token.userId(), "SUCCESS", now);
    }

    @Transactional
    public String requestEmailVerification(String email) {
        String normalizedEmail = normalizeEmail(email);
        attemptLimiter.check("verification", normalizedEmail);
        var account = repository.findForLogin(normalizedEmail);
        if (account.isPresent() && "PENDING_VERIFICATION".equals(account.get().status())) {
            Instant now = clock.instant();
            String rawToken = tokens.generate();
            repository.replaceIdentityToken(account.get().id(), "VERIFY_EMAIL", UUID.randomUUID(),
                    tokens.hash(rawToken), now.plus(properties.verificationTtl()), now);
            messages.sendVerification(normalizedEmail, rawToken);
        }
        return GENERIC_VERIFICATION_MESSAGE;
    }

    @Transactional
    public SessionTokens login(String email, String password) {
        String normalizedEmail = normalizeEmail(email);
        attemptLimiter.check("login", normalizedEmail);
        var account = repository.findForLogin(normalizedEmail);
        String hashToCheck = account.map(IdentityRepository.LoginAccount::passwordHash).orElse(dummyPasswordHash);
        boolean passwordMatches = passwordEncoder.matches(password, hashToCheck);
        if (account.isEmpty() || !passwordMatches || !"ACTIVE".equals(account.get().status())) {
            throw new IdentityException(IdentityException.Kind.UNAUTHORIZED, "INVALID_CREDENTIALS",
                    "E-mail ou senha inválidos.");
        }
        Instant now = clock.instant();
        SessionTokens session = newSession(account.get().id(), now);
        attemptLimiter.clear("login", normalizedEmail);
        audit.record(account.get().id(), "SESSION_STARTED", "SESSION", null, "SUCCESS", now);
        return session;
    }

    @Transactional(noRollbackFor = IdentityException.class)
    public SessionTokens refresh(String rawRefreshToken) {
        if (rawRefreshToken == null || rawRefreshToken.isBlank()) {
            throw invalidSession();
        }
        Instant now = clock.instant();
        var stored = repository.findRefreshToken(tokens.hash(rawRefreshToken)).orElseThrow(this::invalidSession);
        if (stored.consumedAt() != null) {
            repository.revokeFamily(stored.familyId(), "REFRESH_REUSE", now);
            throw invalidSession();
        }
        if (stored.revokedAt() != null || !stored.expiresAt().isAfter(now)) {
            throw invalidSession();
        }
        String access = tokens.generate();
        String refresh = tokens.generate();
        repository.rotateRefresh(stored, UUID.randomUUID(), tokens.hash(access), now.plus(properties.accessTtl()),
                UUID.randomUUID(), tokens.hash(refresh), now.plus(properties.refreshTtl()), now);
        audit.record(stored.userId(), "SESSION_REFRESHED", "SESSION", stored.familyId(), "SUCCESS", now);
        return new SessionTokens(access, refresh);
    }

    @Transactional
    public void logout(AuthenticatedAccount account) {
        Instant now = clock.instant();
        repository.revokeFamily(account.sessionFamilyId(), "LOGOUT", now);
        audit.record(account.userId(), "SESSION_ENDED", "SESSION", account.sessionFamilyId(), "SUCCESS", now);
    }

    @Transactional
    public String requestPasswordRecovery(String email) {
        String normalizedEmail = normalizeEmail(email);
        attemptLimiter.check("recovery", normalizedEmail);
        var account = repository.findForLogin(normalizedEmail);
        if (account.isPresent() && "ACTIVE".equals(account.get().status())) {
            Instant now = clock.instant();
            String rawToken = tokens.generate();
            repository.replaceIdentityToken(account.get().id(), "RESET_PASSWORD", UUID.randomUUID(),
                    tokens.hash(rawToken), now.plus(properties.recoveryTtl()), now);
            messages.sendPasswordRecovery(normalizedEmail, rawToken);
        }
        return GENERIC_RECOVERY_MESSAGE;
    }

    @Transactional
    public void resetPassword(String rawToken, String newPassword) {
        validatePassword(newPassword);
        Instant now = clock.instant();
        var token = validIdentityToken(rawToken, "RESET_PASSWORD", now);
        repository.resetPassword(token, passwordEncoder.encode(newPassword), now);
        repository.revokeAllFamilies(token.userId(), "PASSWORD_RESET", now);
        audit.record(token.userId(), "PASSWORD_RESET", "USER", token.userId(), "SUCCESS", now);
    }

    @Transactional
    public AuthenticatedAccount updateDisplayName(AuthenticatedAccount account, String displayName) {
        validateName(displayName);
        repository.updateDisplayName(account.userId(), displayName.trim(), clock.instant());
        return new AuthenticatedAccount(account.userId(), account.sessionFamilyId(), account.email(), displayName.trim());
    }

    public void verifyPassword(UUID userId, String password) {
        var account = repository.findForLoginByUserId(userId);
        if (account.isEmpty() || !passwordEncoder.matches(password, account.get().passwordHash())) {
            throw new IdentityException(IdentityException.Kind.UNAUTHORIZED, "REAUTHENTICATION_FAILED",
                    "Confirme sua senha para continuar.");
        }
    }

    private SessionTokens newSession(UUID userId, Instant now) {
        String access = tokens.generate();
        String refresh = tokens.generate();
        repository.createSession(UUID.randomUUID(), UUID.randomUUID(), tokens.hash(access),
                now.plus(properties.accessTtl()), UUID.randomUUID(), tokens.hash(refresh),
                now.plus(properties.refreshTtl()), userId, now);
        return new SessionTokens(access, refresh);
    }

    private IdentityRepository.IdentityToken validIdentityToken(String rawToken, String purpose, Instant now) {
        if (rawToken == null || rawToken.isBlank()) throw invalidToken();
        var stored = repository.findIdentityToken(tokens.hash(rawToken)).orElseThrow(this::invalidToken);
        if (!purpose.equals(stored.purpose()) || stored.consumedAt() != null || !stored.expiresAt().isAfter(now)) {
            throw invalidToken();
        }
        return stored;
    }

    private void validatePassword(String password) {
        int length = password == null ? 0 : password.codePointCount(0, password.length());
        if (length < properties.minimumPasswordLength() || length > properties.maximumPasswordLength()) {
            throw new IdentityException(IdentityException.Kind.BAD_REQUEST, "INVALID_PASSWORD",
                    "A senha deve ter entre %d e %d caracteres."
                            .formatted(properties.minimumPasswordLength(), properties.maximumPasswordLength()));
        }
    }

    private static void validateName(String displayName) {
        if (displayName == null || displayName.trim().length() < 2 || displayName.trim().length() > 120) {
            throw new IdentityException(IdentityException.Kind.BAD_REQUEST, "INVALID_DISPLAY_NAME", "Informe como quer ser chamado.");
        }
    }

    private static String normalizeEmail(String email) {
        if (email == null || email.isBlank() || email.length() > 320 || !email.contains("@")) {
            throw new IdentityException(IdentityException.Kind.BAD_REQUEST, "INVALID_EMAIL", "Informe um e-mail válido.");
        }
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private IdentityException invalidSession() {
        return new IdentityException(IdentityException.Kind.UNAUTHORIZED, "INVALID_SESSION", "Sessão inválida ou expirada.");
    }

    private IdentityException invalidToken() {
        return new IdentityException(IdentityException.Kind.BAD_REQUEST, "INVALID_OR_EXPIRED_TOKEN", "Link inválido ou expirado.");
    }
}
