package br.com.financetarget.goals.application;

import br.com.financetarget.audit.application.AuditEventPort;
import br.com.financetarget.config.ProductProperties;
import br.com.financetarget.planning.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Clock;
import java.time.LocalDate;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;

@Service
public class GoalService {
    private final GoalRepository repository;
    private final GoalEngine engine;
    private final Clock clock;
    private final ProductProperties product;
    private final AuditEventPort audit;

    public GoalService(GoalRepository repository, Clock clock, ProductProperties product, AuditEventPort audit) {
        this.repository = repository;
        this.engine = new GoalEngine();
        this.clock = clock;
        this.product = product;
        this.audit = audit;
    }

    @Transactional
    public GoalView create(UUID userId, UUID spaceId, CreateGoal command) {
        var space = repository.findSpace(userId, spaceId, true).orElseThrow(GoalService::notFound);
        String title = normalizeTitle(command.title());
        if (!"HOME_DOWN_PAYMENT".equals(command.goalType())) {
            throw badRequest("UNSUPPORTED_GOAL_TYPE", "Nesta fase, crie uma meta para entrada de imóvel.");
        }
        var input = new GoalProjectionInput(today(), command.targetDate(), command.targetAmount(),
                command.targetValueBasis(), command.initialBalance(), command.annualInflationRate(),
                command.annualReturnRate(), space.declaredMonthlyCapacity(), space.currency(),
                command.contributionTiming());
        GoalProjection projection;
        try {
            projection = engine.project(input);
        } catch (ProjectionException exception) {
            throw badRequest(exception.code(), exception.getMessage());
        }
        var now = clock.instant();
        var goal = new GoalRepository.StoredGoal(UUID.randomUUID(), spaceId, userId, command.goalType(), title,
                command.targetAmount().setScale(2), command.targetValueBasis(), command.targetDate(),
                command.initialBalance().setScale(2), command.annualInflationRate(), command.annualReturnRate(),
                command.contributionTiming(), projection.requiredMonthlyContribution(), space.currency(),
                "ACTIVE", 0, now);
        repository.insertGoal(goal, input, projection, inputHash(input), userId, now);
        audit.record(userId, "GOAL_CREATED", "GOAL", goal.id(), "SUCCESS", now);
        return view(goal, projection);
    }

    public List<GoalView> list(UUID userId, UUID spaceId) {
        repository.findSpace(userId, spaceId, false).orElseThrow(GoalService::notFound);
        return repository.listGoals(userId, spaceId).stream()
                .map(goal -> view(goal, repository.latestProjection(goal.id()))).toList();
    }

    public GoalView find(UUID userId, UUID spaceId, UUID goalId) {
        var goal = repository.findGoal(userId, spaceId, goalId, false).orElseThrow(GoalService::notFound);
        return view(goal, repository.latestProjection(goal.id()));
    }

    @Transactional
    public ContributionResult contribute(UUID userId, UUID spaceId, UUID goalId, String idempotencyKey,
                                         BigDecimal amount, LocalDate date, String note) {
        var goal = repository.findGoal(userId, spaceId, goalId, true).orElseThrow(GoalService::notFound);
        String key = validateIdempotencyKey(idempotencyKey);
        BigDecimal normalizedAmount = positiveMoney(amount);
        LocalDate today = today();
        if (date == null || date.isAfter(today)) {
            throw badRequest("INVALID_CONTRIBUTION_DATE", "A contribuição não pode ter data futura.");
        }
        String normalizedNote = normalizeNote(note);
        var existing = repository.findContribution(goalId, userId, key);
        if (existing.isPresent()) {
            var contribution = existing.get();
            if (contribution.amount().compareTo(normalizedAmount) != 0
                    || !contribution.contributionDate().equals(date)
                    || !java.util.Objects.equals(contribution.note(), normalizedNote)) {
                throw new GoalException(GoalException.Kind.CONFLICT, "IDEMPOTENCY_KEY_REUSED",
                        "Esta chave já foi usada com dados diferentes.");
            }
            return new ContributionResult(contribution, view(goal, repository.latestProjection(goalId)));
        }
        var now = clock.instant();
        var contribution = new GoalRepository.Contribution(UUID.randomUUID(), goalId, userId, normalizedAmount,
                goal.currency(), date, normalizedNote, key, now);
        repository.insertContribution(spaceId, contribution);
        audit.record(userId, "CONTRIBUTION_RECORDED", "GOAL", goalId, "SUCCESS", now);
        return new ContributionResult(contribution, view(goal, repository.latestProjection(goalId)));
    }

    private GoalView view(GoalRepository.StoredGoal goal, GoalProjection projection) {
        BigDecimal contributionTotal = repository.contributionsTotal(goal.id()).setScale(2);
        BigDecimal currentBalance = goal.initialBalance().add(contributionTotal).setScale(2);
        BigDecimal remaining = projection.targetNominal().subtract(currentBalance).max(BigDecimal.ZERO).setScale(2);
        BigDecimal progress = currentBalance.multiply(BigDecimal.valueOf(100))
                .divide(projection.targetNominal(), 2, RoundingMode.HALF_EVEN)
                .min(new BigDecimal("100.00"));
        return new GoalView(goal, projection, currentBalance, remaining, progress,
                repository.listContributions(goal.id()));
    }

    private LocalDate today() {
        return LocalDate.now(clock.withZone(product.businessTimeZone()));
    }

    private static String inputHash(GoalProjectionInput input) {
        String canonical = String.join("|", input.referenceDate().toString(), input.targetDate().toString(),
                decimal(input.targetAmount()), input.targetValueBasis().name(), decimal(input.initialBalance()),
                decimal(input.annualInflationRate()), decimal(input.annualReturnRate()),
                input.declaredMonthlyCapacity() == null ? "" : decimal(input.declaredMonthlyCapacity()),
                input.currency(), input.contributionTiming().name());
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(canonical.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 indisponível.", exception);
        }
    }

    private static String decimal(BigDecimal value) {
        return value.stripTrailingZeros().toPlainString();
    }

    private static BigDecimal positiveMoney(BigDecimal value) {
        if (value == null || value.signum() <= 0 || value.scale() > 2 || value.precision() - value.scale() > 17) {
            throw badRequest("INVALID_MONEY_VALUE", "Use um valor positivo com no máximo duas casas decimais.");
        }
        return value.setScale(2, RoundingMode.UNNECESSARY);
    }

    private static String normalizeTitle(String value) {
        String normalized = value == null ? "" : value.trim().replaceAll("\\s+", " ");
        if (normalized.length() < 2 || normalized.length() > 120) {
            throw badRequest("INVALID_GOAL_TITLE", "Use um título entre 2 e 120 caracteres.");
        }
        return normalized;
    }

    private static String normalizeNote(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String normalized = value.trim().replaceAll("\\s+", " ");
        if (normalized.length() > 240) {
            throw badRequest("INVALID_CONTRIBUTION_NOTE", "A observação deve ter no máximo 240 caracteres.");
        }
        return normalized;
    }

    private static String validateIdempotencyKey(String value) {
        String normalized = value == null ? "" : value.trim();
        if (normalized.isEmpty() || normalized.length() > 128) {
            throw badRequest("INVALID_IDEMPOTENCY_KEY", "Informe uma chave de idempotência válida.");
        }
        return normalized;
    }

    private static GoalException notFound() {
        return new GoalException(GoalException.Kind.NOT_FOUND, "GOAL_NOT_FOUND", "Meta não encontrada.");
    }

    private static GoalException badRequest(String code, String message) {
        return new GoalException(GoalException.Kind.BAD_REQUEST, code, message);
    }

    public record CreateGoal(String goalType, String title, BigDecimal targetAmount,
                             TargetValueBasis targetValueBasis, LocalDate targetDate, BigDecimal initialBalance,
                             BigDecimal annualInflationRate, BigDecimal annualReturnRate,
                             ContributionTiming contributionTiming) {}

    public record GoalView(GoalRepository.StoredGoal goal, GoalProjection projection,
                           BigDecimal currentBalance, BigDecimal remainingAmount, BigDecimal progressPercentage,
                           List<GoalRepository.Contribution> contributions) {}

    public record ContributionResult(GoalRepository.Contribution contribution, GoalView goal) {}
}
