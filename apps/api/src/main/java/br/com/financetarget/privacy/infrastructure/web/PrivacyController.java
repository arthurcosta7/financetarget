package br.com.financetarget.privacy.infrastructure.web;

import br.com.financetarget.identity.application.AuthenticatedAccount;
import br.com.financetarget.privacy.application.PrivacyRepository;
import br.com.financetarget.privacy.application.PrivacyService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/privacy")
public class PrivacyController {
    private final PrivacyService privacy;

    public PrivacyController(PrivacyService privacy) {
        this.privacy = privacy;
    }

    @PostMapping("/exports")
    ResponseEntity<PrivacyRepository.ExportData> export(
            @AuthenticationPrincipal AuthenticatedAccount account,
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @Valid @RequestBody ReauthenticationRequest request) {
        return ResponseEntity.ok().cacheControl(CacheControl.noStore())
                .body(privacy.export(account.userId(), request.password(), idempotencyKey));
    }

    @PostMapping("/deletion-requests")
    ResponseEntity<PrivacyRepository.SubjectRequest> requestDeletion(
            @AuthenticationPrincipal AuthenticatedAccount account,
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @Valid @RequestBody ReauthenticationRequest request) {
        return ResponseEntity.accepted().cacheControl(CacheControl.noStore())
                .body(privacy.requestDeletion(account.userId(), request.password(), idempotencyKey));
    }

    @GetMapping("/deletion-requests/current")
    ResponseEntity<PrivacyRepository.SubjectRequest> deletionStatus(
            @AuthenticationPrincipal AuthenticatedAccount account) {
        return ResponseEntity.ok().cacheControl(CacheControl.noStore())
                .body(privacy.deletionStatus(account.userId()));
    }

    public record ReauthenticationRequest(@NotBlank String password) {}
}
