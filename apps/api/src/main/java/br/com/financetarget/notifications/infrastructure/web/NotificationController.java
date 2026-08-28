package br.com.financetarget.notifications.infrastructure.web;

import br.com.financetarget.identity.application.AuthenticatedAccount;
import br.com.financetarget.notifications.application.NotificationService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/notification-preferences")
public class NotificationController {
    private final NotificationService notifications;

    public NotificationController(NotificationService notifications) { this.notifications = notifications; }

    @GetMapping
    ResponseEntity<PreferencesResponse> get(@AuthenticationPrincipal AuthenticatedAccount account) {
        return ResponseEntity.ok().cacheControl(CacheControl.noStore())
                .body(PreferencesResponse.from(notifications.preferences(account.userId())));
    }

    @PutMapping
    ResponseEntity<PreferencesResponse> update(@AuthenticationPrincipal AuthenticatedAccount account,
                                               @Valid @RequestBody UpdatePreferencesRequest request) {
        var values = Map.of("PLANNING_REMINDERS", request.planningReminders(),
                "PRODUCT_UPDATES", request.productUpdates(), "MARKETING", request.marketing());
        return ResponseEntity.ok().cacheControl(CacheControl.noStore())
                .body(PreferencesResponse.from(notifications.update(account.userId(), values)));
    }

    public record UpdatePreferencesRequest(@NotNull Boolean planningReminders,
                                           @NotNull Boolean productUpdates,
                                           @NotNull Boolean marketing) {}
    public record PreferencesResponse(boolean essential, boolean planningReminders,
                                      boolean productUpdates, boolean marketing) {
        static PreferencesResponse from(Map<String, Boolean> values) {
            return new PreferencesResponse(values.get("ESSENTIAL"), values.get("PLANNING_REMINDERS"),
                    values.get("PRODUCT_UPDATES"), values.get("MARKETING"));
        }
    }
}
