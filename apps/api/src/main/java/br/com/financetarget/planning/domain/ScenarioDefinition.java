package br.com.financetarget.planning.domain;

public record ScenarioDefinition(String key, GoalProjectionInput input) {
    public ScenarioDefinition {
        if (key == null || key.isBlank() || input == null) {
            throw new ProjectionException("INVALID_SCENARIO", "Cenário e premissas são obrigatórios.");
        }
    }
}
