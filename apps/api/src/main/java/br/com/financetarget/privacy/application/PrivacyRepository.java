package br.com.financetarget.privacy.application;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PrivacyRepository {
    record AccountData(UUID id, String email, String displayName, Instant createdAt, Instant emailVerifiedAt) {}
    record ConsentData(String purpose, String documentVersion, String decision, Instant recordedAt) {}
    record FinancialData(String recurringIncome, String essentialExpenses, String initialGoalBalance,
                         String suggestedMonthlyCapacity, String confirmedMonthlyCapacity, String currency,
                         String referenceDate) {}
    record ContributionData(UUID id, String amount, String currency, String contributionDate, String note,
                            Instant createdAt) {}
    record ScenarioData(UUID id, String title, String targetDate, String annualInflationRate,
                        String annualReturnRate, String contributionTiming, String engineVersion,
                        String formulaVersion, Instant createdAt) {}
    record GoalData(UUID id, String title, String goalType, String targetAmount, String currency,
                    String targetValueBasis, String targetDate, String initialBalance,
                    String annualInflationRate, String annualReturnRate, String contributionTiming,
                    String plannedMonthlyContribution, String status, String engineVersion, String formulaVersion,
                    Instant createdAt, List<ContributionData> contributions, List<ScenarioData> scenarios) {}
    record SubscriptionData(String planCode, String status, String provider, Instant updatedAt) {}
    record NotificationPreferenceData(String category, boolean emailEnabled, Instant updatedAt) {}
    record SpaceMembershipData(UUID spaceId, String type, String name, String role, Instant joinedAt) {}
    record SpaceInvitationData(UUID id, UUID spaceId, String direction, String role, String status, Instant createdAt) {}
    record BetaEventData(String eventName, String journeyStage, String outcome, String deviceClass, Instant occurredAt) {}
    record BetaFeedbackData(UUID id, String category, Integer rating, String comment, String status, Instant createdAt) {}
    record ExportData(AccountData account, Optional<FinancialData> financialProfile,
                      List<GoalData> goals, List<ConsentData> consents,
                      Optional<SubscriptionData> subscription,
                      List<NotificationPreferenceData> notificationPreferences,
                      List<SpaceMembershipData> spaceMemberships, List<SpaceInvitationData> spaceInvitations,
                      List<BetaEventData> betaEvents, List<BetaFeedbackData> betaFeedback) {}
    record SubjectRequest(UUID id, String type, String status, Instant createdAt, Instant completedAt) {}

    ExportData exportOwnData(UUID userId);
    Optional<SubjectRequest> findRequest(UUID userId, String type, String idempotencyKey);
    SubjectRequest createRequest(UUID userId, String type, String status, String idempotencyKey, Instant now);
    Optional<SubjectRequest> latestDeletion(UUID userId);
}
