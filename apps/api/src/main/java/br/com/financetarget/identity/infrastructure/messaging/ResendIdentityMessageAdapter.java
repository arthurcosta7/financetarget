package br.com.financetarget.identity.infrastructure.messaging;

import br.com.financetarget.config.ResendProperties;
import br.com.financetarget.identity.application.IdentityMessageDeliveryException;
import br.com.financetarget.identity.application.IdentityMessagePort;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.HtmlUtils;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.http.HttpClient;
import java.util.List;
import java.util.Map;

@Component
@ConditionalOnProperty(name = "app.integrations.resend.enabled", havingValue = "true")
public class ResendIdentityMessageAdapter implements IdentityMessagePort {
    private final ResendProperties properties;
    private final RestClient client;

    public ResendIdentityMessageAdapter(ResendProperties properties, RestClient.Builder builder) {
        this.properties = properties;
        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(properties.connectTimeout())
                .build();
        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
        requestFactory.setReadTimeout(properties.readTimeout());
        this.client = builder
                .requestFactory(requestFactory)
                .baseUrl(properties.endpoint().toString())
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + properties.apiKey())
                .build();
    }

    @Override
    public void sendVerification(String normalizedEmail, String token) {
        String url = link("/verificar-email", token);
        send(normalizedEmail, "Confirme seu e-mail — FinanceTarget",
                verificationHtml(url),
                "Confirme seu e-mail no FinanceTarget: " + url
                        + "\n\nO link expira em 24 horas. Se você não criou esta conta, ignore esta mensagem.");
    }

    @Override
    public void sendPasswordRecovery(String normalizedEmail, String token) {
        String url = link("/redefinir-senha", token);
        send(normalizedEmail, "Redefinição de senha — FinanceTarget",
                recoveryHtml(url),
                "Redefina sua senha do FinanceTarget: " + url
                        + "\n\nO link expira em 30 minutos. Se você não fez esta solicitação, ignore esta mensagem.");
    }

    private void send(String recipient, String subject, String html, String text) {
        Map<String, Object> payload = Map.of(
                "from", properties.fromName() + " <" + properties.fromEmail() + ">",
                "to", List.of(recipient),
                "subject", subject,
                "html", html,
                "text", text
        );
        try {
            client.post().body(payload).retrieve().toBodilessEntity();
        } catch (RuntimeException exception) {
            throw new IdentityMessageDeliveryException(exception);
        }
    }

    private String link(String path, String token) {
        return UriComponentsBuilder.fromUri(properties.frontendBaseUrl())
                .path(path)
                .queryParam("token", token)
                .build()
                .encode()
                .toUriString();
    }

    private static String verificationHtml(String url) {
        return template("Confirme seu e-mail", "Ative sua conta para criar seu espaço pessoal de planejamento.",
                "Confirmar e-mail", url, "Este link expira em 24 horas. Se você não criou esta conta, ignore esta mensagem.");
    }

    private static String recoveryHtml(String url) {
        return template("Redefinição de senha", "Recebemos uma solicitação para criar uma nova senha para sua conta.",
                "Redefinir senha", url, "Este link expira em 30 minutos. Se você não fez esta solicitação, ignore esta mensagem.");
    }

    private static String template(String title, String description, String action, String url, String footnote) {
        String safeUrl = HtmlUtils.htmlEscape(url);
        return """
                <!doctype html>
                <html lang="pt-BR">
                <body style="margin:0;background:#f5f5f5;color:#111;font-family:Arial,sans-serif">
                  <div style="max-width:560px;margin:0 auto;padding:40px 20px">
                    <p style="margin:0 0 32px;font-size:18px;font-weight:700;letter-spacing:-.02em">FinanceTarget</p>
                    <div style="background:#fff;border:1px solid #ddd;padding:32px">
                      <h1 style="margin:0 0 16px;font-size:26px;line-height:1.2">%s</h1>
                      <p style="margin:0 0 28px;color:#444;line-height:1.6">%s</p>
                      <p style="margin:0 0 28px"><a href="%s" style="display:inline-block;background:#111;color:#fff;padding:13px 20px;text-decoration:none;font-weight:700">%s</a></p>
                      <p style="margin:0;color:#666;font-size:13px;line-height:1.5">%s</p>
                    </div>
                  </div>
                </body>
                </html>
                """.formatted(HtmlUtils.htmlEscape(title), HtmlUtils.htmlEscape(description), safeUrl,
                HtmlUtils.htmlEscape(action), HtmlUtils.htmlEscape(footnote));
    }
}
