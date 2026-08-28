package br.com.financetarget.planning.domain;

import java.math.BigDecimal;

public record ScenarioProjection(String key, GoalProjection projection,
                                 BigDecimal requiredContributionDelta,
                                 BigDecimal targetNominalDelta,
                                 int projectionMonthsDelta) {}
