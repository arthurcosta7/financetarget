package br.com.financetarget.identity.application;

import java.time.Duration;
import java.time.Instant;

public interface AuthenticationAttemptRepository {
    int increment(String keyHash, Instant now, Duration window);
    void clear(String keyHash);
}
