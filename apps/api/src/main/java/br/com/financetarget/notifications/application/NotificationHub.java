package br.com.financetarget.notifications.application;

import java.time.Instant;
import java.util.UUID;

public interface NotificationHub {
    record Message(UUID userId, String category, String templateKey, String channel, Instant requestedAt) {}
    record Delivery(String status) {}

    Delivery deliver(Message message);
}
