package br.com.financetarget.integrations.application;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public interface LoyaltyHub {
    record Balance(String programReference, long points, LocalDate earliestExpiration, Instant observedAt) {}

    List<Balance> readBalances(String connectionReference);
}
