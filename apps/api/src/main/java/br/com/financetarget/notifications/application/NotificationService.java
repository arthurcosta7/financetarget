package br.com.financetarget.notifications.application;

import br.com.financetarget.audit.application.AuditEventPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
public class NotificationService {
    private static final Set<String> OPTIONAL = Set.of("PLANNING_REMINDERS", "PRODUCT_UPDATES", "MARKETING");
    private final NotificationRepository repository;
    private final AuditEventPort audit;
    private final Clock clock;

    public NotificationService(NotificationRepository repository, AuditEventPort audit, Clock clock) {
        this.repository = repository;
        this.audit = audit;
        this.clock = clock;
    }

    public Map<String, Boolean> preferences(UUID userId) {
        var result = new LinkedHashMap<String, Boolean>();
        result.put("ESSENTIAL", true);
        OPTIONAL.stream().sorted().forEach(key -> result.put(key, false));
        result.putAll(repository.preferences(userId));
        result.put("ESSENTIAL", true);
        return result;
    }

    @Transactional
    public Map<String, Boolean> update(UUID userId, Map<String, Boolean> requested) {
        if (requested == null || !OPTIONAL.equals(requested.keySet())) {
            throw new IllegalArgumentException("Informe todas as preferências opcionais conhecidas.");
        }
        var now = clock.instant();
        repository.savePreferences(userId, requested, now);
        audit.record(userId, "NOTIFICATION_PREFERENCES_CHANGED", "USER", userId, "SUCCESS", now);
        return preferences(userId);
    }
}
