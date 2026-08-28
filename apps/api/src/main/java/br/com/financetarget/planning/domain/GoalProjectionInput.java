package br.com.financetarget.planning.domain;

import java.math.BigDecimal;
import java.time.LocalDate;

public record GoalProjectionInput(
        LocalDate referenceDate,
        LocalDate targetDate,
        BigDecimal targetAmount,
        TargetValueBasis targetValueBasis,
        BigDecimal initialBalance,
        BigDecimal annualInflationRate,
        BigDecimal annualReturnRate,
        BigDecimal declaredMonthlyCapacity,
        String currency,
        ContributionTiming contributionTiming) {
}
