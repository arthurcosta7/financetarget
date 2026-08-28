package br.com.financetarget.notifications.application;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public interface NotificationRepository {
    Map<String, Boolean> preferences(UUID userId);
    void savePreferences(UUID userId, Map<String, Boolean> preferences, Instant updatedAt);
    boolean enabled(UUID userId, String category);
    void recordIntent(NotificationHub.Message message, String status);
}
