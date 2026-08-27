package br.com.financetarget.identity.infrastructure.messaging;

import br.com.financetarget.identity.application.IdentityMessagePort;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Profile({"dev", "test"})
public class InMemoryIdentityMessageAdapter implements IdentityMessagePort {
    public record Message(String kind, String token, Instant capturedAt) {}

    private final Map<String, Message> messages = new ConcurrentHashMap<>();

    @Override
    public void sendVerification(String normalizedEmail, String token) {
        messages.put(normalizedEmail, new Message("VERIFY_EMAIL", token, Instant.now()));
    }

    @Override
    public void sendPasswordRecovery(String normalizedEmail, String token) {
        messages.put(normalizedEmail, new Message("RESET_PASSWORD", token, Instant.now()));
    }

    public Message latestFor(String normalizedEmail) {
        return messages.get(normalizedEmail);
    }

    public void clear() {
        messages.clear();
    }
}
