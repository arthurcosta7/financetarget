package br.com.financetarget.goals.application;

import br.com.financetarget.planning.domain.ContributionTiming;
import br.com.financetarget.planning.domain.GoalProjection;
import br.com.financetarget.planning.domain.GoalProjectionInput;
import br.com.financetarget.planning.domain.TargetValueBasis;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface GoalRepository {
    record SpaceContext(UUID id, String currency, BigDecimal declaredMonthlyCapacity) {}

    record StoredGoal(UUID id, UUID spaceId, UUID createdBy, String goalType, String title,
                      BigDecimal targetAmount, TargetValueBasis targetValueBasis, LocalDate targetDate,
                      BigDecimal initialBalance, BigDecimal annualInflationRate, BigDecimal annualReturnRate,
                      ContributionTiming contributionTiming, BigDecimal plannedMonthlyContribution,
                      String currency, String status, long version, Instant createdAt) {}

    record Contribution(UUID id, UUID goalId, UUID createdBy, BigDecimal amount, String currency,
                        LocalDate contributionDate, String note, String idempotencyKey, Instant createdAt) {}

    Optional<SpaceContext> findSpace(UUID userId, UUID spaceId, boolean editable);
    Optional<StoredGoal> findGoal(UUID userId, UUID spaceId, UUID goalId, boolean editable);
    List<StoredGoal> listGoals(UUID userId, UUID spaceId);
    void insertGoal(StoredGoal goal, GoalProjectionInput input, GoalProjection projection,
                    String inputHash, UUID actorUserId, Instant now);
    GoalProjection latestProjection(UUID goalId);
    BigDecimal contributionsTotal(UUID goalId);
    List<Contribution> listContributions(UUID goalId);
    Optional<Contribution> findContribution(UUID goalId, UUID actorUserId, String idempotencyKey);
    void insertContribution(UUID spaceId, Contribution contribution);
}
