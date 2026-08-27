package br.com.financetarget.identity.application;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface IdentityRepository {
    record LoginAccount(UUID id, String email, String displayName, String status, String passwordHash) {}
    record IdentityToken(UUID id, UUID userId, String email, String purpose, Instant expiresAt, Instant consumedAt) {}
    record RefreshToken(UUID id, UUID familyId, UUID userId, String email, String displayName,
                        Instant expiresAt, Instant consumedAt, Instant revokedAt) {}

    boolean emailExists(String email);
    Optional<LoginAccount> findForLogin(String email);
    Optional<LoginAccount> findForLoginByUserId(UUID userId);
    Optional<AuthenticatedAccount> findByAccessHash(String tokenHash, Instant now);
    Optional<IdentityToken> findIdentityToken(String tokenHash);
    Optional<RefreshToken> findRefreshToken(String tokenHash);
    boolean createPendingAccount(UUID userId, String email, String displayName, String passwordHash,
                                 UUID tokenId, String tokenHash, Instant tokenExpiresAt, Instant now);
    void replaceIdentityToken(UUID userId, String purpose, UUID tokenId, String tokenHash,
                              Instant expiresAt, Instant now);
    void activateVerifiedAccount(IdentityToken token, Instant now);
    void createSession(UUID familyId, UUID accessId, String accessHash, Instant accessExpiresAt,
                       UUID refreshId, String refreshHash, Instant refreshExpiresAt, UUID userId, Instant now);
    void rotateRefresh(RefreshToken previous, UUID accessId, String accessHash, Instant accessExpiresAt,
                       UUID refreshId, String refreshHash, Instant refreshExpiresAt, Instant now);
    void revokeFamily(UUID familyId, String reason, Instant now);
    void revokeAllFamilies(UUID userId, String reason, Instant now);
    void resetPassword(IdentityToken token, String passwordHash, Instant now);
    void updateDisplayName(UUID userId, String displayName, Instant now);
}
