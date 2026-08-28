package br.com.financetarget.planning.domain;

import java.util.List;

public record ScenarioComparison(ScenarioProjection base, List<ScenarioProjection> alternatives,
                                 String engineVersion) {
    public ScenarioComparison {
        alternatives = List.copyOf(alternatives);
    }
}
