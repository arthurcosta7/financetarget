package br.com.financetarget.integrations.application;

import java.time.Instant;
import java.util.List;

public interface BankingDataHub {
    record Connection(String reference, String status, Instant consentExpiresAt) {}
    record Account(String reference, String type, String currency) {}
    record Snapshot(Connection connection, List<Account> accounts, Instant observedAt) {}

    Snapshot readSnapshot(String connectionReference);
}
