package br.com.financetarget.goals.infrastructure.web;

import br.com.financetarget.goals.application.GoalRepository;
import br.com.financetarget.goals.application.GoalService;
import br.com.financetarget.identity.application.AuthenticatedAccount;
import br.com.financetarget.planning.domain.ContributionTiming;
import br.com.financetarget.planning.domain.GoalProjection;
import br.com.financetarget.planning.domain.TargetValueBasis;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.net.URI;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/planning-spaces/{spaceId}/goals")
public class GoalController {
    private final GoalService goals;

    public GoalController(GoalService goals) {
        this.goals = goals;
    }

    @PostMapping
    ResponseEntity<GoalResponse> create(@AuthenticationPrincipal AuthenticatedAccount account,
                                        @PathVariable UUID spaceId, @Valid @RequestBody CreateGoalRequest request) {
        var created = goals.create(account.userId(), spaceId, request.toCommand());
        return ResponseEntity.created(URI.create("/api/v1/planning-spaces/" + spaceId + "/goals/" + created.goal().id()))
                .cacheControl(CacheControl.noStore()).body(GoalResponse.from(created));
    }

    @GetMapping
    ResponseEntity<List<GoalResponse>> list(@AuthenticationPrincipal AuthenticatedAccount account,
                                            @PathVariable UUID spaceId) {
        return ResponseEntity.ok().cacheControl(CacheControl.noStore())
                .body(goals.list(account.userId(), spaceId).stream().map(GoalResponse::from).toList());
    }

    @GetMapping("/{goalId}")
    ResponseEntity<GoalResponse> find(@AuthenticationPrincipal AuthenticatedAccount account,
                                      @PathVariable UUID spaceId, @PathVariable UUID goalId) {
        return ResponseEntity.ok().cacheControl(CacheControl.noStore())
                .body(GoalResponse.from(goals.find(account.userId(), spaceId, goalId)));
    }

    @PostMapping("/{goalId}/contributions")
    ResponseEntity<ContributionResultResponse> contribute(@AuthenticationPrincipal AuthenticatedAccount account,
                                                           @PathVariable UUID spaceId, @PathVariable UUID goalId,
                                                           @RequestHeader("Idempotency-Key") String idempotencyKey,
                                                           @Valid @RequestBody ContributionRequest request) {
        var result = goals.contribute(account.userId(), spaceId, goalId, idempotencyKey,
                request.amount(), request.contributionDate(), request.note());
        return ResponseEntity.status(201).cacheControl(CacheControl.noStore())
                .body(new ContributionResultResponse(ContributionResponse.from(result.contribution()),
                        GoalResponse.from(result.goal())));
    }

    public record CreateGoalRequest(
            @NotBlank @Pattern(regexp = "HOME_DOWN_PAYMENT|EMERGENCY_RESERVE|VEHICLE|TRAVEL|CUSTOM") String goalType,
            @NotBlank @Size(min = 2, max = 120) String title,
            @NotNull @DecimalMin("0.01") @Digits(integer = 17, fraction = 2) BigDecimal targetAmount,
            @NotNull TargetValueBasis targetValueBasis,
            @NotNull @Future LocalDate targetDate,
            @NotNull @DecimalMin("0.00") @Digits(integer = 17, fraction = 2) BigDecimal initialBalance,
            @NotNull @DecimalMin(value = "-0.99999999") @Digits(integer = 1, fraction = 8) BigDecimal annualInflationRate,
            @NotNull @DecimalMin(value = "-0.99999999") @Digits(integer = 1, fraction = 8) BigDecimal annualReturnRate,
            @NotNull ContributionTiming contributionTiming) {
        GoalService.CreateGoal toCommand() {
            return new GoalService.CreateGoal(goalType, title, targetAmount, targetValueBasis, targetDate,
                    initialBalance, annualInflationRate, annualReturnRate, contributionTiming);
        }
    }

    public record ContributionRequest(
            @NotNull @DecimalMin("0.01") @Digits(integer = 17, fraction = 2) BigDecimal amount,
            @NotNull @PastOrPresent LocalDate contributionDate,
            @Size(max = 240) String note) {}

    public record MoneyResponse(String amount, String currency) {
        static MoneyResponse of(BigDecimal amount, String currency) {
            return new MoneyResponse(amount.toPlainString(), currency);
        }
    }

    public record ProjectionResponse(MoneyResponse targetNominal, MoneyResponse requiredMonthlyContribution,
                                     MoneyResponse projectedValueAtTarget, String estimatedCompletionDate,
                                     MoneyResponse totalContributed, MoneyResponse projectedGrowth,
                                     MoneyResponse shortfallOrSurplus, int projectionMonths, List<String> warnings,
                                     String engineVersion, String formulaVersion) {
        static ProjectionResponse from(GoalProjection projection, String currency) {
            return new ProjectionResponse(MoneyResponse.of(projection.targetNominal(), currency),
                    MoneyResponse.of(projection.requiredMonthlyContribution(), currency),
                    MoneyResponse.of(projection.projectedValueAtTarget(), currency),
                    projection.estimatedCompletionDate().toString(), MoneyResponse.of(projection.totalContributed(), currency),
                    MoneyResponse.of(projection.projectedGrowth(), currency),
                    MoneyResponse.of(projection.shortfallOrSurplus(), currency), projection.projectionMonths(),
                    projection.warnings().stream().map(Enum::name).toList(), projection.engineVersion(),
                    projection.formulaVersion());
        }
    }

    public record ContributionResponse(String id, MoneyResponse amount, String contributionDate, String note,
                                       String createdAt) {
        static ContributionResponse from(GoalRepository.Contribution contribution) {
            return new ContributionResponse(contribution.id().toString(),
                    MoneyResponse.of(contribution.amount(), contribution.currency()),
                    contribution.contributionDate().toString(), contribution.note(), contribution.createdAt().toString());
        }
    }

    public record GoalResponse(String id, String spaceId, String goalType, String title, MoneyResponse targetAmount,
                               String targetValueBasis, String targetDate, MoneyResponse initialBalance,
                               String annualInflationRate, String annualReturnRate, String contributionTiming,
                               String status, long version, ProjectionResponse projection, MoneyResponse currentBalance,
                               MoneyResponse remainingAmount, String progressPercentage,
                               List<ContributionResponse> contributions, String createdAt) {
        static GoalResponse from(GoalService.GoalView view) {
            var goal = view.goal();
            return new GoalResponse(goal.id().toString(), goal.spaceId().toString(), goal.goalType(), goal.title(),
                    MoneyResponse.of(goal.targetAmount(), goal.currency()), goal.targetValueBasis().name(),
                    goal.targetDate().toString(), MoneyResponse.of(goal.initialBalance(), goal.currency()),
                    goal.annualInflationRate().toPlainString(), goal.annualReturnRate().toPlainString(),
                    goal.contributionTiming().name(), goal.status(), goal.version(),
                    ProjectionResponse.from(view.projection(), goal.currency()),
                    MoneyResponse.of(view.currentBalance(), goal.currency()),
                    MoneyResponse.of(view.remainingAmount(), goal.currency()), view.progressPercentage().toPlainString(),
                    view.contributions().stream().map(ContributionResponse::from).toList(), goal.createdAt().toString());
        }
    }

    public record ContributionResultResponse(ContributionResponse contribution, GoalResponse goal) {}
}
