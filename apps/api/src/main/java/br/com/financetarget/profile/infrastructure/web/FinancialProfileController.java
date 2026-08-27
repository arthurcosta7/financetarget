package br.com.financetarget.profile.infrastructure.web;

import br.com.financetarget.identity.application.AuthenticatedAccount;
import br.com.financetarget.profile.application.FinancialProfileRepository;
import br.com.financetarget.profile.application.FinancialProfileService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/v1/onboarding")
public class FinancialProfileController {
    private final FinancialProfileService profiles;

    public FinancialProfileController(FinancialProfileService profiles) {
        this.profiles = profiles;
    }

    @GetMapping("/requirements")
    ResponseEntity<FinancialProfileService.Requirements> requirements() {
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(profiles.requirements());
    }

    @GetMapping("/financial-profile")
    ResponseEntity<ProfileResponse> profile(@AuthenticationPrincipal AuthenticatedAccount account) {
        return profiles.find(account.userId())
                .map(profile -> ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(ProfileResponse.from(profile)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/financial-profile")
    ResponseEntity<ProfileResponse> save(@AuthenticationPrincipal AuthenticatedAccount account,
                                         @Valid @RequestBody SaveProfileRequest request) {
        var saved = profiles.save(account.userId(), request.recurringIncome(), request.essentialExpenses(),
                request.initialGoalBalance(), request.confirmedMonthlyCapacity(), request.termsAccepted(),
                request.privacyNoticeAcknowledged());
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(ProfileResponse.from(saved));
    }

    public record SaveProfileRequest(
            @NotNull @DecimalMin("0.00") @Digits(integer = 17, fraction = 2) BigDecimal recurringIncome,
            @NotNull @DecimalMin("0.00") @Digits(integer = 17, fraction = 2) BigDecimal essentialExpenses,
            @NotNull @DecimalMin("0.00") @Digits(integer = 17, fraction = 2) BigDecimal initialGoalBalance,
            @NotNull @DecimalMin("0.00") @Digits(integer = 17, fraction = 2) BigDecimal confirmedMonthlyCapacity,
            boolean termsAccepted,
            boolean privacyNoticeAcknowledged) {}

    public record ProfileResponse(String spaceId, String recurringIncome, String essentialExpenses,
                                  String initialGoalBalance, String suggestedMonthlyCapacity,
                                  String confirmedMonthlyCapacity, String currency, String referenceDate) {
        static ProfileResponse from(FinancialProfileRepository.Profile profile) {
            return new ProfileResponse(profile.spaceId().toString(), profile.recurringIncome().toPlainString(),
                    profile.essentialExpenses().toPlainString(), profile.initialGoalBalance().toPlainString(),
                    profile.suggestedMonthlyCapacity().toPlainString(),
                    profile.confirmedMonthlyCapacity().toPlainString(), profile.currency(),
                    profile.referenceDate().toString());
        }
    }
}
