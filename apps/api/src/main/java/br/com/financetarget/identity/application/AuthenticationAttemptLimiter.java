package br.com.financetarget.identity.application;

import br.com.financetarget.config.AuthProperties;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class AuthenticationAttemptLimiter {
    private record Window(Instant startedAt, int attempts) {}

    private final ConcurrentHashMap<String, Window> windows = new ConcurrentHashMap<>();
    private final AuthProperties properties;
    private final TokenService tokenService;
    private final Clock clock;

    public AuthenticationAttemptLimiter(AuthProperties properties, TokenService tokenService, Clock clock) {
        this.properties = properties;
        this.tokenService = tokenService;
        this.clock = clock;
    }

    public void check(String action, String normalizedIdentifier) {
        Instant now = clock.instant();
        String key = action + ':' + tokenService.hash(normalizedIdentifier);
        Window window = windows.compute(key, (ignored, current) -> {
            if (current == null || current.startedAt().plus(properties.attemptWindow()).isBefore(now)) {
                return new Window(now, 1);
            }
            return new Window(current.startedAt(), current.attempts() + 1);
        });
        if (window.attempts() > properties.attemptLimit()) {
            throw new IdentityException(IdentityException.Kind.TOO_MANY_REQUESTS, "RATE_LIMITED",
                    "Muitas tentativas. Aguarde um pouco e tente novamente.");
        }
    }

    public void clear(String action, String normalizedIdentifier) {
        windows.remove(action + ':' + tokenService.hash(normalizedIdentifier));
    }
}
