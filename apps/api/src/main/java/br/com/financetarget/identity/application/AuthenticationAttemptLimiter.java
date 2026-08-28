package br.com.financetarget.identity.application;

import br.com.financetarget.config.AuthProperties;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Instant;

@Component
public class AuthenticationAttemptLimiter {
    private final AuthProperties properties;
    private final TokenService tokenService;
    private final AuthenticationAttemptRepository repository;
    private final Clock clock;

    public AuthenticationAttemptLimiter(AuthProperties properties, TokenService tokenService,
                                        AuthenticationAttemptRepository repository, Clock clock) {
        this.properties = properties;
        this.tokenService = tokenService;
        this.repository = repository;
        this.clock = clock;
    }

    public void check(String action, String normalizedIdentifier) {
        Instant now = clock.instant();
        String keyHash = tokenService.hash(action + ':' + normalizedIdentifier);
        int attempts = repository.increment(keyHash, now, properties.attemptWindow());
        if (attempts > properties.attemptLimit()) {
            throw new IdentityException(IdentityException.Kind.TOO_MANY_REQUESTS, "RATE_LIMITED",
                    "Muitas tentativas. Aguarde um pouco e tente novamente.");
        }
    }

    public void clear(String action, String normalizedIdentifier) {
        repository.clear(tokenService.hash(action + ':' + normalizedIdentifier));
    }
}
