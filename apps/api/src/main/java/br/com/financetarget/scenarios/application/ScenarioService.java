package br.com.financetarget.scenarios.application;

import br.com.financetarget.audit.application.AuditEventPort;
import br.com.financetarget.config.ProductProperties;
import br.com.financetarget.goals.application.GoalException;
import br.com.financetarget.planning.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class ScenarioService {
    private final ScenarioRepository repository;
    private final ScenarioEngine engine = new ScenarioEngine();
    private final Clock clock;
    private final ProductProperties product;
    private final AuditEventPort audit;

    public ScenarioService(ScenarioRepository repository, Clock clock, ProductProperties product, AuditEventPort audit) {
        this.repository = repository;
        this.clock = clock;
        this.product = product;
        this.audit = audit;
    }

    public ComparisonView compare(UUID userId, UUID spaceId, UUID goalId) {
        var context = repository.findContext(userId, spaceId, goalId, false).orElseThrow(ScenarioService::notFound);
        return view(context, repository.list(goalId));
    }

    @Transactional
    public ComparisonView create(UUID userId, UUID spaceId, UUID goalId, CreateScenario command) {
        var context = repository.findContext(userId, spaceId, goalId, true).orElseThrow(ScenarioService::notFound);
        List<ScenarioRepository.StoredScenario> existing = repository.list(goalId);
        if (existing.size() >= 3) {
            throw badRequest("SCENARIO_LIMIT_REACHED", "Compare no máximo três cenários nesta fase.");
        }
        String title = normalizeTitle(command.title());
        var goal = context.goal();
        LocalDate referenceDate = LocalDate.ofInstant(goal.createdAt(), product.businessTimeZone());
        var baseInput = input(referenceDate, goal.targetDate(), goal.annualInflationRate(), goal.annualReturnRate(),
                goal.contributionTiming(), context);
        var alternativeInput = input(referenceDate, command.targetDate(), command.annualInflationRate(),
                command.annualReturnRate(), command.contributionTiming(), context);
        ScenarioComparison comparison;
        try {
            comparison = engine.compare(new ScenarioDefinition("BASE", baseInput),
                    List.of(new ScenarioDefinition(title, alternativeInput)));
        } catch (ProjectionException exception) {
            throw badRequest(exception.code(), exception.getMessage());
        }
        var now = clock.instant();
        var stored = new ScenarioRepository.StoredScenario(UUID.randomUUID(), spaceId, goalId, userId, title,
                command.targetDate(), command.annualInflationRate(), command.annualReturnRate(),
                command.contributionTiming(), comparison.alternatives().getFirst().projection(), now);
        repository.insert(stored, alternativeInput, ProjectionHasher.hash(alternativeInput));
        audit.record(userId, "SCENARIO_CREATED", "GOAL", goalId, "SUCCESS", now);
        existing = new java.util.ArrayList<>(existing);
        existing.add(stored);
        return view(context, existing);
    }

    private static GoalProjectionInput input(LocalDate referenceDate, LocalDate targetDate, BigDecimal inflation,
                                             BigDecimal returnRate, ContributionTiming timing,
                                             ScenarioRepository.Context context) {
        var goal = context.goal();
        return new GoalProjectionInput(referenceDate, targetDate, goal.targetAmount(), goal.targetValueBasis(),
                goal.initialBalance(), inflation, returnRate, context.declaredMonthlyCapacity(), goal.currency(), timing);
    }

    private static ComparisonView view(ScenarioRepository.Context context,
                                       List<ScenarioRepository.StoredScenario> scenarios) {
        GoalProjection base = context.baseProjection();
        List<ScenarioView> alternatives = scenarios.stream().map(scenario -> new ScenarioView(scenario,
                scenario.projection().requiredMonthlyContribution().subtract(base.requiredMonthlyContribution()),
                scenario.projection().targetNominal().subtract(base.targetNominal()),
                scenario.projection().projectionMonths() - base.projectionMonths())).toList();
        return new ComparisonView(base, alternatives, context.goal().currency(), ScenarioEngine.ENGINE_VERSION);
    }

    private static String normalizeTitle(String value) {
        String normalized = value == null ? "" : value.trim().replaceAll("\\s+", " ");
        if (normalized.length() < 2 || normalized.length() > 80) {
            throw badRequest("INVALID_SCENARIO_TITLE", "Use um nome entre 2 e 80 caracteres.");
        }
        return normalized;
    }

    private static GoalException notFound() {
        return new GoalException(GoalException.Kind.NOT_FOUND, "GOAL_NOT_FOUND", "Meta não encontrada.");
    }

    private static GoalException badRequest(String code, String message) {
        return new GoalException(GoalException.Kind.BAD_REQUEST, code, message);
    }

    public record CreateScenario(String title, LocalDate targetDate, BigDecimal annualInflationRate,
                                 BigDecimal annualReturnRate, ContributionTiming contributionTiming) {}
    public record ScenarioView(ScenarioRepository.StoredScenario scenario, BigDecimal requiredContributionDelta,
                               BigDecimal targetNominalDelta, int projectionMonthsDelta) {}
    public record ComparisonView(GoalProjection base, List<ScenarioView> scenarios, String currency,
                                 String scenarioEngineVersion) {}
}
