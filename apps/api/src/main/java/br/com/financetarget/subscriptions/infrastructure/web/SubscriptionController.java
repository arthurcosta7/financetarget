package br.com.financetarget.subscriptions.infrastructure.web;

import br.com.financetarget.identity.application.AuthenticatedAccount;
import br.com.financetarget.subscriptions.application.SubscriptionRepository;
import br.com.financetarget.subscriptions.application.SubscriptionService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/subscriptions")
public class SubscriptionController {
    private final SubscriptionService subscriptions;

    public SubscriptionController(SubscriptionService subscriptions) { this.subscriptions = subscriptions; }

    @GetMapping("/current")
    ResponseEntity<OverviewResponse> current(@AuthenticationPrincipal AuthenticatedAccount account) {
        return ResponseEntity.ok().cacheControl(CacheControl.noStore())
                .body(OverviewResponse.from(subscriptions.overview(account.userId())));
    }

    @PostMapping("/mock-checkouts")
    ResponseEntity<CheckoutResponse> checkout(@AuthenticationPrincipal AuthenticatedAccount account,
                                              @RequestHeader("Idempotency-Key") String idempotencyKey,
                                              @Valid @RequestBody CheckoutRequest request) {
        var result = subscriptions.createCheckout(account.userId(), request.planCode(), idempotencyKey);
        return ResponseEntity.created(URI.create("/api/v1/subscriptions/mock-checkouts/" + result.id()))
                .cacheControl(CacheControl.noStore()).body(CheckoutResponse.from(result));
    }

    public record CheckoutRequest(@NotBlank @Size(max = 64) String planCode) {}
    public record SubscriptionResponse(String planCode, String status, String provider,
                                       long version, String updatedAt) {
        static SubscriptionResponse from(SubscriptionRepository.Subscription value) {
            return new SubscriptionResponse(value.planCode(), value.status(), value.provider(), value.version(),
                    value.updatedAt().toString());
        }
    }
    public record PlanResponse(String code, String displayName, Map<String, String> entitlements) {
        static PlanResponse from(SubscriptionRepository.Plan value) {
            return new PlanResponse(value.code(), value.displayName(), value.entitlements());
        }
    }
    public record OverviewResponse(SubscriptionResponse subscription, Map<String, String> entitlements,
                                   List<PlanResponse> availablePlans, boolean mockCheckoutEnabled) {
        static OverviewResponse from(SubscriptionService.Overview value) {
            return new OverviewResponse(value.subscription().map(SubscriptionResponse::from).orElse(null),
                    value.entitlements(), value.availablePlans().stream().map(PlanResponse::from).toList(),
                    value.mockCheckoutEnabled());
        }
    }
    public record CheckoutResponse(String id, String planCode, String provider, String reference,
                                   String status, String createdAt) {
        static CheckoutResponse from(SubscriptionService.CheckoutView value) {
            return new CheckoutResponse(value.id().toString(), value.planCode(), value.provider(),
                    value.reference(), value.status(), value.createdAt().toString());
        }
    }
}
