package br.com.financetarget.profile.application;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

public interface FinancialProfileRepository {
    record Profile(UUID spaceId, BigDecimal recurringIncome, BigDecimal essentialExpenses,
                   BigDecimal initialGoalBalance, BigDecimal suggestedMonthlyCapacity,
                   BigDecimal confirmedMonthlyCapacity, String currency, LocalDate referenceDate) {}

    Optional<Profile> findPersonalProfile(UUID userId);
    UUID requirePersonalSpace(UUID userId);
    void save(Profile profile, Instant now);
    void recordConsent(UUID userId, String purpose, String documentVersion, Instant now);
}
