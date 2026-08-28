package br.com.financetarget.identity.infrastructure.web;

import br.com.financetarget.identity.infrastructure.messaging.InMemoryIdentityMessageAdapter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Locale;

@RestController
@Profile("dev")
@ConditionalOnProperty(name = "app.integrations.resend.enabled", havingValue = "false", matchIfMissing = true)
@RequestMapping("/api/v1/dev/identity-messages")
public class DevIdentityMessageController {
    private final InMemoryIdentityMessageAdapter messages;

    public DevIdentityMessageController(InMemoryIdentityMessageAdapter messages) {
        this.messages = messages;
    }

    @GetMapping("/latest")
    ResponseEntity<InMemoryIdentityMessageAdapter.Message> latest(@RequestParam String email) {
        var message = messages.latestFor(email.trim().toLowerCase(Locale.ROOT));
        return message == null ? ResponseEntity.notFound().build() : ResponseEntity.ok()
                .cacheControl(CacheControl.noStore()).body(message);
    }
}
