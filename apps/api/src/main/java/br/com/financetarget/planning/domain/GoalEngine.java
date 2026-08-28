package br.com.financetarget.planning.domain;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;

public final class GoalEngine {
    public static final String ENGINE_VERSION = "goal-engine-1";
    public static final String FORMULA_VERSION = "monthly-annuity-1";
    private static final MathContext CALCULATION_CONTEXT = MathContext.DECIMAL128;
    private static final int MONEY_SCALE = 2;

    public GoalProjection project(GoalProjectionInput input) {
        validate(input);
        int months = Math.toIntExact(ChronoUnit.MONTHS.between(
                YearMonth.from(input.referenceDate()), YearMonth.from(input.targetDate())));

        BigDecimal inflationFactor = monthlyFactor(input.annualInflationRate());
        BigDecimal returnFactor = monthlyFactor(input.annualReturnRate());
        BigDecimal targetNominal = input.targetValueBasis() == TargetValueBasis.CURRENT_VALUE
                ? input.targetAmount().multiply(inflationFactor.pow(months, CALCULATION_CONTEXT), CALCULATION_CONTEXT)
                : input.targetAmount();
        BigDecimal accumulatedInitialBalance = input.initialBalance()
                .multiply(returnFactor.pow(months, CALCULATION_CONTEXT), CALCULATION_CONTEXT);
        BigDecimal annuityFactor = annuityFactor(returnFactor, months, input.contributionTiming());
        BigDecimal remaining = targetNominal.subtract(accumulatedInitialBalance, CALCULATION_CONTEXT);
        BigDecimal requiredContribution = remaining.signum() <= 0
                ? BigDecimal.ZERO
                : remaining.divide(annuityFactor, CALCULATION_CONTEXT);
        requiredContribution = money(requiredContribution);

        BigDecimal projectedValue = accumulatedInitialBalance.add(
                requiredContribution.multiply(annuityFactor, CALCULATION_CONTEXT), CALCULATION_CONTEXT);
        BigDecimal totalContributed = input.initialBalance().add(
                requiredContribution.multiply(BigDecimal.valueOf(months), CALCULATION_CONTEXT), CALCULATION_CONTEXT);
        BigDecimal projectedGrowth = projectedValue.subtract(totalContributed, CALCULATION_CONTEXT);
        BigDecimal shortfallOrSurplus = projectedValue.subtract(targetNominal, CALCULATION_CONTEXT);

        var warnings = new ArrayList<ProjectionWarning>();
        if (remaining.signum() <= 0) {
            warnings.add(ProjectionWarning.TARGET_ALREADY_FUNDED);
        }
        if (input.annualReturnRate().signum() < 0) {
            warnings.add(ProjectionWarning.NEGATIVE_RETURN_ASSUMPTION);
        }
        if (input.annualInflationRate().signum() == 0) {
            warnings.add(ProjectionWarning.INFLATION_NOT_INCLUDED);
        }
        if (input.declaredMonthlyCapacity() != null
                && requiredContribution.compareTo(input.declaredMonthlyCapacity()) > 0) {
            warnings.add(ProjectionWarning.CONTRIBUTION_EXCEEDS_DECLARED_CAPACITY);
        }
        warnings.add(ProjectionWarning.FEES_NOT_INCLUDED);
        warnings.add(ProjectionWarning.TAXES_NOT_INCLUDED);
        warnings.add(ProjectionWarning.PROJECTION_NOT_GUARANTEE);

        return new GoalProjection(money(targetNominal), requiredContribution, money(projectedValue),
                input.targetDate(), money(totalContributed), money(projectedGrowth), money(shortfallOrSurplus),
                months, java.util.List.copyOf(warnings), ENGINE_VERSION, FORMULA_VERSION);
    }

    private static BigDecimal monthlyFactor(BigDecimal annualRate) {
        return DecimalMath.nthRoot(BigDecimal.ONE.add(annualRate, CALCULATION_CONTEXT), 12, CALCULATION_CONTEXT);
    }

    private static BigDecimal annuityFactor(BigDecimal monthlyFactor, int months, ContributionTiming timing) {
        BigDecimal monthlyRate = monthlyFactor.subtract(BigDecimal.ONE, CALCULATION_CONTEXT);
        BigDecimal factor = monthlyRate.signum() == 0
                ? BigDecimal.valueOf(months)
                : monthlyFactor.pow(months, CALCULATION_CONTEXT).subtract(BigDecimal.ONE, CALCULATION_CONTEXT)
                        .divide(monthlyRate, CALCULATION_CONTEXT);
        return timing == ContributionTiming.BEGINNING_OF_MONTH
                ? factor.multiply(monthlyFactor, CALCULATION_CONTEXT)
                : factor;
    }

    private static BigDecimal money(BigDecimal value) {
        return value.setScale(MONEY_SCALE, RoundingMode.HALF_EVEN);
    }

    private static void validate(GoalProjectionInput input) {
        if (input == null || input.referenceDate() == null || input.targetDate() == null
                || input.targetAmount() == null || input.targetValueBasis() == null
                || input.initialBalance() == null || input.annualInflationRate() == null
                || input.annualReturnRate() == null || input.currency() == null
                || input.currency().isBlank() || input.contributionTiming() == null) {
            throw new ProjectionException("INCOMPLETE_PROJECTION_INPUT", "Informe todas as premissas obrigatórias.");
        }
        long months = ChronoUnit.MONTHS.between(
                YearMonth.from(input.referenceDate()), YearMonth.from(input.targetDate()));
        if (months < 1 || months > 1_200) {
            throw new ProjectionException("INVALID_TARGET_DATE",
                    "A data da meta deve estar entre um mês e cem anos após a data-base.");
        }
        if (input.targetAmount().signum() <= 0 || input.initialBalance().signum() < 0
                || input.declaredMonthlyCapacity() != null && input.declaredMonthlyCapacity().signum() < 0) {
            throw new ProjectionException("INVALID_MONEY_VALUE", "Valores monetários devem respeitar os limites da meta.");
        }
        if (input.targetAmount().scale() > MONEY_SCALE || input.initialBalance().scale() > MONEY_SCALE
                || input.declaredMonthlyCapacity() != null && input.declaredMonthlyCapacity().scale() > MONEY_SCALE) {
            throw new ProjectionException("INVALID_MONEY_SCALE", "Use no máximo duas casas decimais para dinheiro.");
        }
        if (input.annualInflationRate().compareTo(BigDecimal.ONE.negate()) <= 0
                || input.annualReturnRate().compareTo(BigDecimal.ONE.negate()) <= 0) {
            throw new ProjectionException("INVALID_ANNUAL_RATE", "Taxas anuais devem ser maiores que -100%. ");
        }
        if (!input.currency().matches("[A-Z]{3}")) {
            throw new ProjectionException("INVALID_CURRENCY", "Informe a moeda no padrão ISO 4217.");
        }
    }
}
