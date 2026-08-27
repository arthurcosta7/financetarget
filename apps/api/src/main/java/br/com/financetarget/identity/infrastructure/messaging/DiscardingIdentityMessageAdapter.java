package br.com.financetarget.identity.infrastructure.messaging;

import br.com.financetarget.identity.application.IdentityMessagePort;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("!dev & !test")
public class DiscardingIdentityMessageAdapter implements IdentityMessagePort {
    @Override
    public void sendVerification(String normalizedEmail, String token) {
        // A Fase 3 não autoriza entrega real. O token não é registrado em logs.
    }

    @Override
    public void sendPasswordRecovery(String normalizedEmail, String token) {
        // A Fase 3 não autoriza entrega real. O token não é registrado em logs.
    }
}
