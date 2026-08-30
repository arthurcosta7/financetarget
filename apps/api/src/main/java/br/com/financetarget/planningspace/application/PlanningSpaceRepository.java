package br.com.financetarget.planningspace.application;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PlanningSpaceRepository {
    record Space(UUID id, String type, String name, String baseCurrency, String role, int memberCount,
                 boolean profileConfigured) {}
    record Invitation(UUID id, UUID spaceId, String spaceName, String inviterName, String recipientEmail,
                      String role, String status, Instant expiresAt, Instant createdAt) {}
    record Member(UUID userId, String displayName, String role, Instant joinedAt) {}
    record SharedProfile(UUID spaceId, BigDecimal recurringIncome, BigDecimal essentialExpenses,
                         BigDecimal initialGoalBalance, BigDecimal suggestedMonthlyCapacity,
                         BigDecimal confirmedMonthlyCapacity, String currency, LocalDate referenceDate) {}

    List<Space> listSpaces(UUID userId);
    void insertSharedSpace(UUID spaceId, UUID ownerId, String name, String currency, Instant now);
    boolean isOwner(UUID userId, UUID spaceId);
    boolean canEdit(UUID userId, UUID spaceId);
    int activeMemberCount(UUID spaceId);
    int invitationCountSince(UUID actorId, Instant since);
    boolean pendingInvitationExists(UUID spaceId, String email, Instant now);
    void insertInvitation(Invitation invitation, UUID actorId);
    List<Invitation> listPendingInvitations(String recipientEmail, Instant now);
    Optional<Invitation> lockInvitation(UUID invitationId, String recipientEmail);
    void acceptInvitation(UUID invitationId, UUID spaceId, UUID userId, String role, Instant now);
    void rejectInvitation(UUID invitationId, Instant now);
    List<Member> listMembers(UUID userId, UUID spaceId);
    Optional<String> memberRole(UUID spaceId, UUID memberId);
    int ownerCount(UUID spaceId);
    void updateMemberRole(UUID spaceId, UUID memberId, String role);
    Optional<SharedProfile> findProfile(UUID userId, UUID spaceId);
    void saveProfile(SharedProfile profile, Instant now);
}
