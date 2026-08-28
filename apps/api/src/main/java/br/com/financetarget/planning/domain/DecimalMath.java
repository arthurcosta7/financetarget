package br.com.financetarget.planning.domain;

import java.math.BigDecimal;
import java.math.MathContext;

final class DecimalMath {
    private static final BigDecimal TWO = BigDecimal.valueOf(2);

    private DecimalMath() {
    }

    static BigDecimal nthRoot(BigDecimal value, int degree, MathContext context) {
        if (value.signum() <= 0 || degree < 1) {
            throw new IllegalArgumentException("A raiz exige valor positivo e grau válido.");
        }
        if (degree == 1 || value.compareTo(BigDecimal.ONE) == 0) {
            return value;
        }

        BigDecimal degreeValue = BigDecimal.valueOf(degree);
        BigDecimal degreeMinusOne = BigDecimal.valueOf(degree - 1L);
        BigDecimal estimate = value.compareTo(BigDecimal.ONE) >= 0
                ? value.add(BigDecimal.ONE, context).divide(TWO, context)
                : BigDecimal.ONE;
        BigDecimal tolerance = BigDecimal.ONE.scaleByPowerOfTen(-context.getPrecision() + 4);

        for (int iteration = 0; iteration < 100; iteration++) {
            BigDecimal denominator = estimate.pow(degree - 1, context);
            BigDecimal next = degreeMinusOne.multiply(estimate, context)
                    .add(value.divide(denominator, context), context)
                    .divide(degreeValue, context);
            if (next.subtract(estimate, context).abs().compareTo(tolerance) <= 0) {
                return next;
            }
            estimate = next;
        }
        throw new ArithmeticException("A raiz decimal não convergiu no limite definido.");
    }
}
