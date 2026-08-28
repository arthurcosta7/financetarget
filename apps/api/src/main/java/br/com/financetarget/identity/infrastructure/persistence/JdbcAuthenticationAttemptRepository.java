package br.com.financetarget.identity.infrastructure.persistence;

import br.com.financetarget.identity.application.AuthenticationAttemptRepository;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Propagation;

import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Repository
public class JdbcAuthenticationAttemptRepository implements AuthenticationAttemptRepository {
    private final JdbcClient jdbc;

    public JdbcAuthenticationAttemptRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public int increment(String keyHash, Instant now, Duration window) {
        OffsetDateTime current = dbTime(now);
        OffsetDateTime expiresAt = dbTime(now.plus(window));
        jdbc.sql("delete from authentication_attempt_window where expires_at < :now")
                .param("now", current).update();
        return jdbc.sql("""
                        insert into authentication_attempt_window(key_hash,window_started_at,expires_at,attempts)
                        values (:keyHash,:now,:expiresAt,1)
                        on conflict (key_hash) do update set
                            window_started_at=case when authentication_attempt_window.expires_at <= :now
                                then :now else authentication_attempt_window.window_started_at end,
                            expires_at=case when authentication_attempt_window.expires_at <= :now
                                then :expiresAt else authentication_attempt_window.expires_at end,
                            attempts=case when authentication_attempt_window.expires_at <= :now
                                then 1 else authentication_attempt_window.attempts+1 end
                        returning attempts
                        """).param("keyHash", keyHash).param("now", current).param("expiresAt", expiresAt)
                .query(Integer.class).single();
    }

    @Override
    public void clear(String keyHash) {
        jdbc.sql("delete from authentication_attempt_window where key_hash=:keyHash")
                .param("keyHash", keyHash).update();
    }

    private static OffsetDateTime dbTime(Instant value) {
        return OffsetDateTime.ofInstant(value, ZoneOffset.UTC);
    }
}
