package br.com.financetarget.scenarios.application;

import br.com.financetarget.goals.application.GoalRepository;
import br.com.financetarget.planning.domain.ContributionTiming;
import br.com.financetarget.planning.domain.GoalProjection;
import br.com.financetarget.planning.domain.GoalProjectionInput;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ScenarioRepository {
    record Context(GoalRepository.StoredGoal goal, BigDecimal declaredMonthlyCapacity,
                   GoalProjection baseProjection) {}

    record StoredScenario(UUID id, UUID spaceId, UUID goalId, UUID createdBy, String title,
                          LocalDate targetDate, BigDecimal annualInflationRate, BigDecimal annualReturnRate,
                          ContributionTiming contributionTiming, GoalProjection projection, Instant createdAt) {}

    Optional<Context> findContext(UUID userId, UUID spaceId, UUID goalId, boolean editable);
    List<StoredScenario> list(UUID goalId);
    void insert(StoredScenario scenario, GoalProjectionInput input, String inputHash);
}
