package br.com.financetarget.planning.domain;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

public final class ProjectionHasher {
    private ProjectionHasher() {}

    public static String hash(GoalProjectionInput input) {
        String canonical = String.join("|", input.referenceDate().toString(), input.targetDate().toString(),
                decimal(input.targetAmount()), input.targetValueBasis().name(), decimal(input.initialBalance()),
                decimal(input.annualInflationRate()), decimal(input.annualReturnRate()),
                input.declaredMonthlyCapacity() == null ? "" : decimal(input.declaredMonthlyCapacity()),
                input.currency(), input.contributionTiming().name());
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(canonical.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 indisponível.", exception);
        }
    }

    private static String decimal(java.math.BigDecimal value) {
        return value.stripTrailingZeros().toPlainString();
    }
}
