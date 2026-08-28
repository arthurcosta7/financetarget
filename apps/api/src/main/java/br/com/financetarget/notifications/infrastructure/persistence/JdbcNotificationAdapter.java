package br.com.financetarget.notifications.infrastructure.persistence;

import br.com.financetarget.config.FeatureFlagProperties;
import br.com.financetarget.notifications.application.NotificationHub;
import br.com.financetarget.notifications.application.NotificationRepository;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Repository
public class JdbcNotificationAdapter implements NotificationRepository, NotificationHub {
    private final JdbcClient jdbc;
    private final FeatureFlagProperties features;

    public JdbcNotificationAdapter(JdbcClient jdbc, FeatureFlagProperties features) {
        this.jdbc = jdbc;
        this.features = features;
    }

    @Override
    public Map<String, Boolean> preferences(UUID userId) {
        var result = new LinkedHashMap<String, Boolean>();
        jdbc.sql("""
                        select category,email_enabled from notification_preference
                        where user_id=:userId order by category
                        """).param("userId", userId).query((rs, row) -> Map.entry(
                        rs.getString("category"), rs.getBoolean("email_enabled")))
                .list().forEach(item -> result.put(item.getKey(), item.getValue()));
        return result;
    }

    @Override
    public void savePreferences(UUID userId, Map<String, Boolean> preferences, Instant updatedAt) {
        preferences.forEach((category, enabled) -> jdbc.sql("""
                        insert into notification_preference(user_id,category,email_enabled,updated_at)
                        values (:userId,:category,:enabled,:updatedAt)
                        on conflict (user_id,category) do update
                        set email_enabled=excluded.email_enabled,updated_at=excluded.updated_at
                        """).param("userId", userId).param("category", category).param("enabled", enabled)
                .param("updatedAt", dbTime(updatedAt)).update());
    }

    @Override
    public boolean enabled(UUID userId, String category) {
        if ("ESSENTIAL".equals(category)) return true;
        return jdbc.sql("""
                        select email_enabled from notification_preference
                        where user_id=:userId and category=:category
                        """).param("userId", userId).param("category", category).query(Boolean.class)
                .optional().orElse(false);
    }

    @Override
    public Delivery deliver(Message message) {
        String status = !features.notificationsMock() ? "DISABLED"
                : enabled(message.userId(), message.category()) ? "SIMULATED" : "SUPPRESSED";
        recordIntent(message, status);
        return new Delivery(status);
    }

    @Override
    public void recordIntent(Message message, String status) {
        jdbc.sql("""
                        insert into notification_intent(id,user_id,category,template_key,channel,status,created_at)
                        values (:id,:userId,:category,:templateKey,:channel,:status,:createdAt)
                        """).param("id", UUID.randomUUID()).param("userId", message.userId())
                .param("category", message.category()).param("templateKey", message.templateKey())
                .param("channel", message.channel()).param("status", status)
                .param("createdAt", dbTime(message.requestedAt())).update();
    }

    private static OffsetDateTime dbTime(Instant value) {
        return OffsetDateTime.ofInstant(value, ZoneOffset.UTC);
    }
}
