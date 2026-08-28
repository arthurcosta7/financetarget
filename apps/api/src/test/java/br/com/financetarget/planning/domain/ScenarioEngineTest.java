package br.com.financetarget.planning.domain;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ScenarioEngineTest {
    private final ScenarioEngine engine = new ScenarioEngine();

    @Test
    void comparesAlternativesAgainstTheSameExplicitBase() {
        var base = new ScenarioDefinition("BASE", input("2028-01-01", "0", "0"));
        var later = new ScenarioDefinition("MAIS_TEMPO", input("2029-01-01", "0", "0"));
        var result = engine.compare(base, List.of(later));
        assertThat(result.engineVersion()).isEqualTo("scenario-engine-1");
        assertThat(result.base().requiredContributionDelta()).isEqualByComparingTo("0.00");
        assertThat(result.alternatives()).hasSize(1);
        assertThat(result.alternatives().getFirst().requiredContributionDelta()).isNegative();
        assertThat(result.alternatives().getFirst().projectionMonthsDelta()).isEqualTo(12);
    }

    @Test
    void rejectsEmptyOrOversizedComparisons() {
        var base = new ScenarioDefinition("BASE", input("2028-01-01", "0", "0"));
        assertThatThrownBy(() -> engine.compare(base, List.of())).isInstanceOf(ProjectionException.class)
                .hasMessageContaining("um e três");
        assertThatThrownBy(() -> engine.compare(base, List.of(base, base, base, base)))
                .isInstanceOf(ProjectionException.class).hasMessageContaining("um e três");
    }

    private static GoalProjectionInput input(String date, String inflation, String returnRate) {
        return new GoalProjectionInput(LocalDate.of(2027, 1, 1), LocalDate.parse(date),
                new BigDecimal("12000.00"), TargetValueBasis.FIXED_NOMINAL, BigDecimal.ZERO,
                new BigDecimal(inflation), new BigDecimal(returnRate), new BigDecimal("1000.00"), "BRL",
                ContributionTiming.END_OF_MONTH);
    }
}
