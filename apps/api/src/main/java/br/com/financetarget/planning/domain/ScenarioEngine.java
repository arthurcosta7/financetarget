package br.com.financetarget.planning.domain;

import java.util.List;

public final class ScenarioEngine {
    public static final String ENGINE_VERSION = "scenario-engine-1";

    private final GoalEngine goals = new GoalEngine();

    public ScenarioComparison compare(ScenarioDefinition base, List<ScenarioDefinition> alternatives) {
        if (base == null || alternatives == null || alternatives.isEmpty() || alternatives.size() > 3) {
            throw new ProjectionException("INVALID_SCENARIO_SET", "Compare entre um e três cenários por vez.");
        }
        GoalProjection baseProjection = goals.project(base.input());
        var baseResult = new ScenarioProjection(base.key(), baseProjection,
                baseProjection.requiredMonthlyContribution().subtract(baseProjection.requiredMonthlyContribution()),
                baseProjection.targetNominal().subtract(baseProjection.targetNominal()), 0);
        List<ScenarioProjection> results = alternatives.stream().map(definition -> {
            GoalProjection projection = goals.project(definition.input());
            return new ScenarioProjection(definition.key(), projection,
                    projection.requiredMonthlyContribution().subtract(baseProjection.requiredMonthlyContribution()),
                    projection.targetNominal().subtract(baseProjection.targetNominal()),
                    projection.projectionMonths() - baseProjection.projectionMonths());
        }).toList();
        return new ScenarioComparison(baseResult, results, ENGINE_VERSION);
    }
}
