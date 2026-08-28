package br.com.financetarget.planning.domain;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record GoalProjection(
        BigDecimal targetNominal,
        BigDecimal requiredMonthlyContribution,
        BigDecimal projectedValueAtTarget,
        LocalDate estimatedCompletionDate,
        BigDecimal totalContributed,
        BigDecimal projectedGrowth,
        BigDecimal shortfallOrSurplus,
        int projectionMonths,
        List<ProjectionWarning> warnings,
        String engineVersion,
        String formulaVersion) {
}
