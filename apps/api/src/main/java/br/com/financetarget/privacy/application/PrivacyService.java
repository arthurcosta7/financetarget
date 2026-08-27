package br.com.financetarget.privacy.application;

import br.com.financetarget.identity.application.IdentityException;
import br.com.financetarget.identity.application.IdentityService;
import br.com.financetarget.audit.application.AuditEventPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.util.UUID;

@Service
public class PrivacyService {
    private final PrivacyRepository privacy;
    private final AuditEventPort audit;
    private final IdentityService identityService;
    private final Clock clock;

    public PrivacyService(PrivacyRepository privacy, AuditEventPort audit,
                          IdentityService identityService, Clock clock) {
        this.privacy = privacy;
        this.audit = audit;
        this.identityService = identityService;
        this.clock = clock;
    }

    @Transactional
    public PrivacyRepository.ExportData export(UUID userId, String password, String idempotencyKey) {
        validateKey(idempotencyKey);
        identityService.verifyPassword(userId, password);
        var now = clock.instant();
        privacy.createRequest(userId, "EXPORT", "COMPLETED", idempotencyKey, now);
        audit.record(userId, "PERSONAL_DATA_EXPORTED", "USER", userId, "SUCCESS", now);
        return privacy.exportOwnData(userId);
    }

    @Transactional
    public PrivacyRepository.SubjectRequest requestDeletion(UUID userId, String password, String idempotencyKey) {
        validateKey(idempotencyKey);
        identityService.verifyPassword(userId, password);
        var now = clock.instant();
        var request = privacy.createRequest(userId, "DELETION", "REQUESTED", idempotencyKey, now);
        audit.record(userId, "DELETION_REQUESTED", "DATA_SUBJECT_REQUEST", request.id(), "SUCCESS", now);
        return request;
    }

    public PrivacyRepository.SubjectRequest deletionStatus(UUID userId) {
        return privacy.latestDeletion(userId).orElseThrow(() -> new IdentityException(IdentityException.Kind.NOT_FOUND,
                "DELETION_REQUEST_NOT_FOUND", "Nenhuma solicitação de exclusão foi encontrada."));
    }

    private static void validateKey(String key) {
        if (key == null || key.isBlank() || key.length() > 128) {
            throw new IdentityException(IdentityException.Kind.BAD_REQUEST, "INVALID_IDEMPOTENCY_KEY",
                    "Informe uma chave de idempotência válida.");
        }
    }
}
