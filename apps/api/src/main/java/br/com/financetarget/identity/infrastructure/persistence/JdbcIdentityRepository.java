package br.com.financetarget.identity.infrastructure.persistence;

import br.com.financetarget.identity.application.AuthenticatedAccount;
import br.com.financetarget.identity.application.IdentityRepository;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Repository
public class JdbcIdentityRepository implements IdentityRepository {
    private final JdbcClient jdbc;

    public JdbcIdentityRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public boolean emailExists(String email) {
        return jdbc.sql("select count(*) from app_user where email_normalized = :email")
                .param("email", email).query(Integer.class).single() > 0;
    }

    @Override
    public Optional<LoginAccount> findForLogin(String email) {
        return jdbc.sql("""
                        select u.id, u.email_normalized, u.display_name, u.status, c.password_hash
                        from app_user u join credential c on c.user_id = u.id
                        where u.email_normalized = :email
                        """)
                .param("email", email)
                .query((rs, row) -> new LoginAccount(
                        rs.getObject("id", UUID.class), rs.getString("email_normalized"),
                        rs.getString("display_name"), rs.getString("status"), rs.getString("password_hash")))
                .optional();
    }

    @Override
    public Optional<LoginAccount> findForLoginByUserId(UUID userId) {
        return jdbc.sql("""
                        select u.id, u.email_normalized, u.display_name, u.status, c.password_hash
                        from app_user u join credential c on c.user_id = u.id
                        where u.id = :userId
                        """)
                .param("userId", userId)
                .query((rs, row) -> new LoginAccount(
                        rs.getObject("id", UUID.class), rs.getString("email_normalized"),
                        rs.getString("display_name"), rs.getString("status"), rs.getString("password_hash")))
                .optional();
    }

    @Override
    public Optional<AuthenticatedAccount> findByAccessHash(String tokenHash, Instant now) {
        return jdbc.sql("""
                        select u.id, f.id as family_id, u.email_normalized, u.display_name
                        from access_token t
                        join session_family f on f.id = t.family_id
                        join app_user u on u.id = f.user_id
                        where t.token_hash = :hash and t.expires_at > :now
                          and f.revoked_at is null and u.status = 'ACTIVE'
                        """)
                .param("hash", tokenHash).param("now", dbTime(now))
                .query((rs, row) -> new AuthenticatedAccount(
                        rs.getObject("id", UUID.class), rs.getObject("family_id", UUID.class),
                        rs.getString("email_normalized"), rs.getString("display_name")))
                .optional();
    }

    @Override
    public Optional<IdentityToken> findIdentityToken(String tokenHash) {
        return jdbc.sql("""
                        select t.id, t.user_id, u.email_normalized, t.purpose, t.expires_at, t.consumed_at
                        from identity_token t join app_user u on u.id = t.user_id
                        where t.token_hash = :hash
                        for update
                        """)
                .param("hash", tokenHash)
                .query((rs, row) -> new IdentityToken(
                        rs.getObject("id", UUID.class), rs.getObject("user_id", UUID.class),
                        rs.getString("email_normalized"), rs.getString("purpose"),
                        rs.getTimestamp("expires_at").toInstant(),
                        rs.getTimestamp("consumed_at") == null ? null : rs.getTimestamp("consumed_at").toInstant()))
                .optional();
    }

    @Override
    public Optional<RefreshToken> findRefreshToken(String tokenHash) {
        return jdbc.sql("""
                        select t.id, t.family_id, f.user_id, u.email_normalized, u.display_name,
                               t.expires_at, t.consumed_at, f.revoked_at
                        from refresh_token t
                        join session_family f on f.id = t.family_id
                        join app_user u on u.id = f.user_id
                        where t.token_hash = :hash
                        for update
                        """)
                .param("hash", tokenHash)
                .query((rs, row) -> new RefreshToken(
                        rs.getObject("id", UUID.class), rs.getObject("family_id", UUID.class),
                        rs.getObject("user_id", UUID.class), rs.getString("email_normalized"),
                        rs.getString("display_name"), rs.getTimestamp("expires_at").toInstant(),
                        rs.getTimestamp("consumed_at") == null ? null : rs.getTimestamp("consumed_at").toInstant(),
                        rs.getTimestamp("revoked_at") == null ? null : rs.getTimestamp("revoked_at").toInstant()))
                .optional();
    }

    @Override
    public boolean createPendingAccount(UUID userId, String email, String displayName, String passwordHash,
                                        UUID tokenId, String tokenHash, Instant tokenExpiresAt, Instant now) {
        int inserted = jdbc.sql("""
                        insert into app_user values (:id,:email,:name,'PENDING_VERIFICATION',null,:now,:now)
                        on conflict (email_normalized) do nothing
                        """).param("id", userId).param("email", email).param("name", displayName).param("now", dbTime(now)).update();
        if (inserted == 0) return false;
        jdbc.sql("insert into credential(user_id,password_hash,password_version,changed_at) values (:id,:hash,1,:now)")
                .param("id", userId).param("hash", passwordHash).param("now", dbTime(now)).update();
        insertIdentityToken(userId, "VERIFY_EMAIL", tokenId, tokenHash, tokenExpiresAt, now);
        return true;
    }

    @Override
    public void replaceIdentityToken(UUID userId, String purpose, UUID tokenId, String tokenHash,
                                     Instant expiresAt, Instant now) {
        jdbc.sql("update identity_token set consumed_at=:now where user_id=:userId and purpose=:purpose and consumed_at is null")
                .param("now", dbTime(now)).param("userId", userId).param("purpose", purpose).update();
        insertIdentityToken(userId, purpose, tokenId, tokenHash, expiresAt, now);
    }

    private void insertIdentityToken(UUID userId, String purpose, UUID tokenId, String tokenHash,
                                     Instant expiresAt, Instant now) {
        jdbc.sql("""
                        insert into identity_token(id,user_id,purpose,token_hash,expires_at,created_at)
                        values (:id,:userId,:purpose,:hash,:expiresAt,:now)
                        """)
                .param("id", tokenId).param("userId", userId).param("purpose", purpose)
                .param("hash", tokenHash).param("expiresAt", dbTime(expiresAt)).param("now", dbTime(now)).update();
    }

    @Override
    public void activateVerifiedAccount(IdentityToken token, Instant now) {
        int consumed = jdbc.sql("update identity_token set consumed_at=:now where id=:id and consumed_at is null")
                .param("now", dbTime(now)).param("id", token.id()).update();
        if (consumed != 1) throw new IllegalStateException("Token já consumido");
        jdbc.sql("update app_user set status='ACTIVE', email_verified_at=:now, updated_at=:now where id=:id")
                .param("now", dbTime(now)).param("id", token.userId()).update();
    }

    @Override
    public void createSession(UUID familyId, UUID accessId, String accessHash, Instant accessExpiresAt,
                              UUID refreshId, String refreshHash, Instant refreshExpiresAt, UUID userId, Instant now) {
        jdbc.sql("insert into session_family(id,user_id,created_at,last_seen_at) values (:id,:userId,:now,:now)")
                .param("id", familyId).param("userId", userId).param("now", dbTime(now)).update();
        insertAccess(familyId, accessId, accessHash, accessExpiresAt, now);
        insertRefresh(familyId, refreshId, refreshHash, refreshExpiresAt, now);
    }

    @Override
    public void rotateRefresh(RefreshToken previous, UUID accessId, String accessHash, Instant accessExpiresAt,
                              UUID refreshId, String refreshHash, Instant refreshExpiresAt, Instant now) {
        int changed = jdbc.sql("update refresh_token set consumed_at=:now where id=:id and consumed_at is null")
                .param("now", dbTime(now)).param("id", previous.id()).update();
        if (changed != 1) throw new IllegalStateException("Refresh já consumido");
        jdbc.sql("delete from access_token where family_id=:familyId")
                .param("familyId", previous.familyId()).update();
        jdbc.sql("update session_family set last_seen_at=:now where id=:id")
                .param("now", dbTime(now)).param("id", previous.familyId()).update();
        insertAccess(previous.familyId(), accessId, accessHash, accessExpiresAt, now);
        insertRefresh(previous.familyId(), refreshId, refreshHash, refreshExpiresAt, now);
    }

    private void insertAccess(UUID familyId, UUID id, String hash, Instant expiresAt, Instant now) {
        jdbc.sql("insert into access_token(id,family_id,token_hash,expires_at,created_at) values (:id,:familyId,:hash,:expiresAt,:now)")
                .param("id", id).param("familyId", familyId).param("hash", hash)
                .param("expiresAt", dbTime(expiresAt)).param("now", dbTime(now)).update();
    }

    private void insertRefresh(UUID familyId, UUID id, String hash, Instant expiresAt, Instant now) {
        jdbc.sql("insert into refresh_token(id,family_id,token_hash,expires_at,created_at) values (:id,:familyId,:hash,:expiresAt,:now)")
                .param("id", id).param("familyId", familyId).param("hash", hash)
                .param("expiresAt", dbTime(expiresAt)).param("now", dbTime(now)).update();
    }

    @Override
    public void revokeFamily(UUID familyId, String reason, Instant now) {
        jdbc.sql("update session_family set revoked_at=:now, revocation_reason=:reason where id=:id and revoked_at is null")
                .param("now", dbTime(now)).param("reason", reason).param("id", familyId).update();
    }

    @Override
    public void revokeAllFamilies(UUID userId, String reason, Instant now) {
        jdbc.sql("update session_family set revoked_at=:now, revocation_reason=:reason where user_id=:userId and revoked_at is null")
                .param("now", dbTime(now)).param("reason", reason).param("userId", userId).update();
    }

    @Override
    public void resetPassword(IdentityToken token, String passwordHash, Instant now) {
        jdbc.sql("update identity_token set consumed_at=:now where id=:id and consumed_at is null")
                .param("now", dbTime(now)).param("id", token.id()).update();
        jdbc.sql("update credential set password_hash=:hash,password_version=password_version+1,changed_at=:now where user_id=:id")
                .param("hash", passwordHash).param("now", dbTime(now)).param("id", token.userId()).update();
    }

    @Override
    public void updateDisplayName(UUID userId, String displayName, Instant now) {
        jdbc.sql("update app_user set display_name=:name,updated_at=:now where id=:id")
                .param("name", displayName).param("now", dbTime(now)).param("id", userId).update();
    }

    private static java.time.OffsetDateTime dbTime(Instant value) {
        return java.time.OffsetDateTime.ofInstant(value, java.time.ZoneOffset.UTC);
    }
}
