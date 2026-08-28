package br.com.financetarget.identity.infrastructure.messaging;

import br.com.financetarget.config.ResendProperties;
import br.com.financetarget.identity.application.IdentityMessageDeliveryException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ResendIdentityMessageAdapterTest {
    private final ObjectMapper json = new ObjectMapper();
    private HttpServer server;

    @AfterEach
    void stopServer() {
        if (server != null) server.stop(0);
    }

    @Test
    void sendsCanonicalVerificationRequestWithoutExposingProviderToThePort() throws Exception {
        AtomicReference<String> authorization = new AtomicReference<>();
        AtomicReference<String> body = new AtomicReference<>();
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/emails", exchange -> {
            authorization.set(exchange.getRequestHeaders().getFirst("Authorization"));
            body.set(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
            byte[] response = "{\"id\":\"synthetic-email-id\"}".getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, response.length);
            exchange.getResponseBody().write(response);
            exchange.close();
        });
        server.start();

        adapter(server.getAddress().getPort()).sendVerification("ana@example.test", "opaque-token");

        assertThat(authorization.get()).isEqualTo("Bearer re_synthetic_only");
        JsonNode payload = json.readTree(body.get());
        assertThat(payload.path("from").asText()).isEqualTo("FinanceTarget <noreply@example.test>");
        assertThat(payload.path("to").get(0).asText()).isEqualTo("ana@example.test");
        assertThat(payload.path("html").asText())
                .contains("http://localhost:3000/verificar-email?token=opaque-token")
                .doesNotContain("Redator");
    }

    @Test
    void mapsProviderFailureToAnInternalDeliveryError() throws Exception {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/emails", exchange -> {
            exchange.sendResponseHeaders(429, -1);
            exchange.close();
        });
        server.start();

        assertThatThrownBy(() -> adapter(server.getAddress().getPort())
                .sendVerification("ana@example.test", "opaque-token"))
                .isInstanceOf(IdentityMessageDeliveryException.class);
    }

    private ResendIdentityMessageAdapter adapter(int port) {
        var properties = new ResendProperties(true, "re_synthetic_only",
                URI.create("http://127.0.0.1:" + port + "/emails"), "noreply@example.test", "FinanceTarget",
                URI.create("http://localhost:3000"), Duration.ofSeconds(2), Duration.ofSeconds(2));
        return new ResendIdentityMessageAdapter(properties, RestClient.builder());
    }
}
