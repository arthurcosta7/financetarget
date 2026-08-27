package br.com.financetarget.identity.infrastructure.web;

import br.com.financetarget.config.AuthProperties;
import br.com.financetarget.config.OpaqueAccessTokenFilter;
import br.com.financetarget.identity.application.AuthenticatedAccount;
import br.com.financetarget.identity.application.IdentityService;
import br.com.financetarget.identity.application.SessionTokens;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/auth")
public class IdentityController {
    private final IdentityService identity;
    private final AuthProperties properties;

    public IdentityController(IdentityService identity, AuthProperties properties) {
        this.identity = identity;
        this.properties = properties;
    }

    @GetMapping("/csrf")
    ResponseEntity<Map<String, String>> csrf(CsrfToken token) {
        return ResponseEntity.ok().cacheControl(CacheControl.noStore())
                .body(Map.of("headerName", token.getHeaderName(), "token", token.getToken()));
    }

    @PostMapping("/registrations")
    ResponseEntity<MessageResponse> register(@Valid @RequestBody RegistrationRequest request) {
        return ResponseEntity.accepted().cacheControl(CacheControl.noStore())
                .body(new MessageResponse(identity.register(request.email(), request.displayName(), request.password())));
    }

    @PostMapping("/verifications")
    ResponseEntity<Void> verify(@Valid @RequestBody TokenRequest request) {
        identity.verifyEmail(request.token());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/sessions")
    ResponseEntity<Void> login(@Valid @RequestBody LoginRequest request) {
        SessionTokens session = identity.login(request.email(), request.password());
        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, accessCookie(session.accessToken()).toString())
                .header(HttpHeaders.SET_COOKIE, refreshCookie(session.refreshToken()).toString()).build();
    }

    @PostMapping("/sessions/refresh")
    ResponseEntity<Void> refresh(HttpServletRequest request) {
        String refresh = OpaqueAccessTokenFilter.cookie(request, properties.refreshCookieName());
        SessionTokens session = identity.refresh(refresh);
        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, accessCookie(session.accessToken()).toString())
                .header(HttpHeaders.SET_COOKIE, refreshCookie(session.refreshToken()).toString()).build();
    }

    @DeleteMapping("/sessions/current")
    ResponseEntity<Void> logout(@AuthenticationPrincipal AuthenticatedAccount account) {
        identity.logout(account);
        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, sessionCookie(properties.accessCookieName(), "", java.time.Duration.ZERO, "/").toString())
                .header(HttpHeaders.SET_COOKIE, sessionCookie(properties.refreshCookieName(), "", java.time.Duration.ZERO,
                        "/api/v1/auth").toString()).build();
    }

    @GetMapping("/me")
    ResponseEntity<AccountResponse> me(@AuthenticationPrincipal AuthenticatedAccount account) {
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(AccountResponse.from(account));
    }

    @PatchMapping("/me")
    ResponseEntity<AccountResponse> updateMe(@AuthenticationPrincipal AuthenticatedAccount account,
                                             @Valid @RequestBody UpdateAccountRequest request) {
        return ResponseEntity.ok().cacheControl(CacheControl.noStore())
                .body(AccountResponse.from(identity.updateDisplayName(account, request.displayName())));
    }

    @PostMapping("/password-recovery-requests")
    ResponseEntity<MessageResponse> requestRecovery(@Valid @RequestBody RecoveryRequest request) {
        return ResponseEntity.accepted().cacheControl(CacheControl.noStore())
                .body(new MessageResponse(identity.requestPasswordRecovery(request.email())));
    }

    @PostMapping("/password-recoveries")
    ResponseEntity<Void> recover(@Valid @RequestBody ResetPasswordRequest request) {
        identity.resetPassword(request.token(), request.newPassword());
        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, sessionCookie(properties.accessCookieName(), "", java.time.Duration.ZERO, "/").toString())
                .header(HttpHeaders.SET_COOKIE, sessionCookie(properties.refreshCookieName(), "", java.time.Duration.ZERO,
                        "/api/v1/auth").toString()).build();
    }

    private ResponseCookie accessCookie(String value) {
        return sessionCookie(properties.accessCookieName(), value, properties.accessTtl(), "/");
    }

    private ResponseCookie refreshCookie(String value) {
        return sessionCookie(properties.refreshCookieName(), value, properties.refreshTtl(), "/api/v1/auth");
    }

    private ResponseCookie sessionCookie(String name, String value, java.time.Duration maxAge, String path) {
        return ResponseCookie.from(name, value).httpOnly(true).secure(properties.secureCookies())
                .sameSite("Lax").path(path).maxAge(maxAge).build();
    }

    public record RegistrationRequest(@NotBlank @Email String email, @NotBlank String displayName,
                                      @NotBlank String password) {}
    public record LoginRequest(@NotBlank @Email String email, @NotBlank String password) {}
    public record TokenRequest(@NotBlank String token) {}
    public record RecoveryRequest(@NotBlank @Email String email) {}
    public record ResetPasswordRequest(@NotBlank String token, @NotBlank String newPassword) {}
    public record UpdateAccountRequest(@NotBlank String displayName) {}
    public record MessageResponse(String message) {}
    public record AccountResponse(UUID id, String email, String displayName) {
        static AccountResponse from(AuthenticatedAccount account) {
            return new AccountResponse(account.userId(), account.email(), account.displayName());
        }
    }
}
