package br.com.financetarget.planning.domain;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GoalEngineTest {
    private final GoalEngine engine = new GoalEngine();

    @Test
    void calculatesLinearContributionWhenRatesAreZero() {
        var result = engine.project(input("120000.00", "24000.00", "0", "0", "5000.00",
                TargetValueBasis.FIXED_NOMINAL, ContributionTiming.END_OF_MONTH));

        assertThat(result.projectionMonths()).isEqualTo(48);
        assertThat(result.requiredMonthlyContribution()).isEqualByComparingTo("2000.00");
        assertThat(result.targetNominal()).isEqualByComparingTo("120000.00");
        assertThat(result.projectedValueAtTarget()).isEqualByComparingTo("120000.00");
    }

    @Test
    void updatesCurrentValueTargetByAnnualInflation() {
        var result = engine.project(new GoalProjectionInput(LocalDate.of(2026, 8, 1), LocalDate.of(2027, 8, 1),
                new BigDecimal("100000.00"), TargetValueBasis.CURRENT_VALUE, BigDecimal.ZERO,
                new BigDecimal("0.12"), BigDecimal.ZERO, null, "BRL", ContributionTiming.END_OF_MONTH));

        assertThat(result.targetNominal()).isEqualByComparingTo("112000.00");
    }

    @Test
    void returnsZeroWhenInitialBalanceAlreadyFundsTarget() {
        var result = engine.project(input("120000.00", "120000.00", "0", "0", null,
                TargetValueBasis.FIXED_NOMINAL, ContributionTiming.END_OF_MONTH));

        assertThat(result.requiredMonthlyContribution()).isZero();
        assertThat(result.warnings()).contains(ProjectionWarning.TARGET_ALREADY_FUNDED);
    }

    @Test
    void beginningOfMonthNeedsLessThanEndOfMonthWithPositiveReturn() {
        var beginning = engine.project(input("120000.00", "10000.00", "0", "0.08", null,
                TargetValueBasis.FIXED_NOMINAL, ContributionTiming.BEGINNING_OF_MONTH));
        var end = engine.project(input("120000.00", "10000.00", "0", "0.08", null,
                TargetValueBasis.FIXED_NOMINAL, ContributionTiming.END_OF_MONTH));

        assertThat(beginning.requiredMonthlyContribution()).isLessThan(end.requiredMonthlyContribution());
    }

    @Test
    void exposesCapacityAndNegativeReturnWarnings() {
        var result = engine.project(input("120000.00", "0.00", "0", "-0.05", "100.00",
                TargetValueBasis.FIXED_NOMINAL, ContributionTiming.END_OF_MONTH));

        assertThat(result.warnings()).contains(ProjectionWarning.NEGATIVE_RETURN_ASSUMPTION,
                ProjectionWarning.CONTRIBUTION_EXCEEDS_DECLARED_CAPACITY,
                ProjectionWarning.PROJECTION_NOT_GUARANTEE);
    }

    @Test
    void rejectsTargetDateInReferenceMonth() {
        var invalid = new GoalProjectionInput(LocalDate.of(2026, 8, 1), LocalDate.of(2026, 8, 31),
                new BigDecimal("100.00"), TargetValueBasis.FIXED_NOMINAL, BigDecimal.ZERO,
                BigDecimal.ZERO, BigDecimal.ZERO, null, "BRL", ContributionTiming.END_OF_MONTH);

        assertThatThrownBy(() -> engine.project(invalid))
                .isInstanceOf(ProjectionException.class)
                .extracting("code").isEqualTo("INVALID_TARGET_DATE");
    }

    private static GoalProjectionInput input(String target, String initial, String inflation, String annualReturn,
                                               String capacity, TargetValueBasis basis, ContributionTiming timing) {
        return new GoalProjectionInput(LocalDate.of(2026, 8, 1), LocalDate.of(2030, 8, 1),
                new BigDecimal(target), basis, new BigDecimal(initial), new BigDecimal(inflation),
                new BigDecimal(annualReturn), capacity == null ? null : new BigDecimal(capacity), "BRL", timing);
    }
}
