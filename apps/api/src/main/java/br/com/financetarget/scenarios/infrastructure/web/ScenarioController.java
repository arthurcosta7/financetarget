package br.com.financetarget.scenarios.infrastructure.web;

import br.com.financetarget.identity.application.AuthenticatedAccount;
import br.com.financetarget.planning.domain.ContributionTiming;
import br.com.financetarget.planning.domain.GoalProjection;
import br.com.financetarget.scenarios.application.ScenarioService;
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
@RequestMapping("/api/v1/planning-spaces/{spaceId}/goals/{goalId}/scenarios")
public class ScenarioController {
    private final ScenarioService scenarios;

    public ScenarioController(ScenarioService scenarios) { this.scenarios = scenarios; }

    @GetMapping
    ResponseEntity<ComparisonResponse> compare(@AuthenticationPrincipal AuthenticatedAccount account,
                                               @PathVariable UUID spaceId, @PathVariable UUID goalId) {
        return ResponseEntity.ok().cacheControl(CacheControl.noStore())
                .body(ComparisonResponse.from(scenarios.compare(account.userId(), spaceId, goalId)));
    }

    @PostMapping
    ResponseEntity<ComparisonResponse> create(@AuthenticationPrincipal AuthenticatedAccount account,
                                              @PathVariable UUID spaceId, @PathVariable UUID goalId,
                                              @Valid @RequestBody CreateScenarioRequest request) {
        var result = scenarios.create(account.userId(), spaceId, goalId, request.toCommand());
        return ResponseEntity.created(URI.create("/api/v1/planning-spaces/" + spaceId + "/goals/" + goalId + "/scenarios"))
                .cacheControl(CacheControl.noStore()).body(ComparisonResponse.from(result));
    }

    public record CreateScenarioRequest(@NotBlank @Size(min = 2, max = 80) String title,
            @NotNull @Future LocalDate targetDate,
            @NotNull @DecimalMin(value = "-0.99999999") @Digits(integer = 1, fraction = 8) BigDecimal annualInflationRate,
            @NotNull @DecimalMin(value = "-0.99999999") @Digits(integer = 1, fraction = 8) BigDecimal annualReturnRate,
            @NotNull ContributionTiming contributionTiming) {
        ScenarioService.CreateScenario toCommand() {
            return new ScenarioService.CreateScenario(title, targetDate, annualInflationRate, annualReturnRate,
                    contributionTiming);
        }
    }

    public record MoneyResponse(String amount, String currency) {
        static MoneyResponse of(BigDecimal value, String currency) { return new MoneyResponse(value.toPlainString(), currency); }
    }

    public record ProjectionResponse(MoneyResponse targetNominal, MoneyResponse requiredMonthlyContribution,
            String estimatedCompletionDate, int projectionMonths, List<String> warnings,
            String engineVersion, String formulaVersion) {
        static ProjectionResponse from(GoalProjection projection, String currency) {
            return new ProjectionResponse(MoneyResponse.of(projection.targetNominal(), currency),
                    MoneyResponse.of(projection.requiredMonthlyContribution(), currency),
                    projection.estimatedCompletionDate().toString(), projection.projectionMonths(),
                    projection.warnings().stream().map(Enum::name).toList(), projection.engineVersion(),
                    projection.formulaVersion());
        }
    }

    public record ScenarioResponse(String id, String title, String targetDate, String annualInflationRate,
            String annualReturnRate, String contributionTiming, ProjectionResponse projection,
            MoneyResponse requiredContributionDelta, MoneyResponse targetNominalDelta,
            int projectionMonthsDelta, String createdAt) {
        static ScenarioResponse from(ScenarioService.ScenarioView view, String currency) {
            var scenario = view.scenario();
            return new ScenarioResponse(scenario.id().toString(), scenario.title(), scenario.targetDate().toString(),
                    scenario.annualInflationRate().toPlainString(), scenario.annualReturnRate().toPlainString(),
                    scenario.contributionTiming().name(), ProjectionResponse.from(scenario.projection(), currency),
                    MoneyResponse.of(view.requiredContributionDelta(), currency),
                    MoneyResponse.of(view.targetNominalDelta(), currency), view.projectionMonthsDelta(),
                    scenario.createdAt().toString());
        }
    }

    public record ComparisonResponse(ProjectionResponse base, List<ScenarioResponse> scenarios,
                                     String scenarioEngineVersion) {
        static ComparisonResponse from(ScenarioService.ComparisonView view) {
            return new ComparisonResponse(ProjectionResponse.from(view.base(), view.currency()),
                    view.scenarios().stream().map(item -> ScenarioResponse.from(item, view.currency())).toList(),
                    view.scenarioEngineVersion());
        }
    }
}
