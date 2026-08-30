package br.com.financetarget.planningspace.application;

import br.com.financetarget.audit.application.AuditEventPort;
import br.com.financetarget.config.BetaProperties;
import br.com.financetarget.config.ProductProperties;
import br.com.financetarget.identity.application.IdentityException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
public class PlanningSpaceService {
    private static final List<String> ROLES = List.of("OWNER", "EDITOR", "VIEWER");
    private final PlanningSpaceRepository spaces;
    private final ProductProperties product;
    private final BetaProperties beta;
    private final AuditEventPort audit;
    private final Clock clock;

    public PlanningSpaceService(PlanningSpaceRepository spaces, ProductProperties product, BetaProperties beta,
                                AuditEventPort audit, Clock clock) {
        this.spaces = spaces;
        this.product = product;
        this.beta = beta;
        this.audit = audit;
        this.clock = clock;
    }

    public List<PlanningSpaceRepository.Space> list(UUID userId) {
        return spaces.listSpaces(userId);
    }

    @Transactional
    public PlanningSpaceRepository.Space createShared(UUID userId, String name) {
        String normalizedName = text(name, 2, 80, "INVALID_SPACE_NAME", "Use um nome entre 2 e 80 caracteres.");
        UUID id = UUID.randomUUID();
        var now = clock.instant();
        spaces.insertSharedSpace(id, userId, normalizedName, product.defaultCurrency(), now);
        audit.record(userId, "SHARED_SPACE_CREATED", "PLANNING_SPACE", id, "SUCCESS", now);
        return spaces.listSpaces(userId).stream().filter(space -> space.id().equals(id)).findFirst().orElseThrow();
    }

    @Transactional
    public PlanningSpaceRepository.Invitation invite(UUID actorId, String actorEmail, UUID spaceId,
                                                      String email, String role) {
        requireOwner(actorId, spaceId);
        String recipient = normalizeEmail(email);
        if (recipient.equals(actorEmail.toLowerCase(Locale.ROOT))) {
            throw bad("CANNOT_INVITE_SELF", "Escolha o e-mail da outra pessoa.");
        }
        String normalizedRole = normalizeRole(role);
        var now = clock.instant();
        if (spaces.invitationCountSince(actorId, now.minus(1, ChronoUnit.DAYS)) >= beta.invitationDailyLimit()) {
            throw new IdentityException(IdentityException.Kind.TOO_MANY_REQUESTS, "INVITATION_RATE_LIMITED",
                    "Limite de convites atingido. Tente novamente mais tarde.");
        }
        if (spaces.activeMemberCount(spaceId) >= beta.maximumSharedMembers()) {
            throw bad("SHARED_SPACE_MEMBER_LIMIT", "Este espaço já atingiu o limite de participantes do beta.");
        }
        if (spaces.pendingInvitationExists(spaceId, recipient, now)) {
            throw bad("INVITATION_ALREADY_PENDING", "Já existe um convite pendente para este e-mail.");
        }
        var invitation = new PlanningSpaceRepository.Invitation(UUID.randomUUID(), spaceId, null, null, recipient,
                normalizedRole, "PENDING", now.plus(beta.invitationTtl()), now);
        spaces.insertInvitation(invitation, actorId);
        audit.record(actorId, "SPACE_MEMBER_INVITED", "PLANNING_SPACE", spaceId, "SUCCESS", now);
        return invitation;
    }

    public List<PlanningSpaceRepository.Invitation> invitations(String email) {
        return spaces.listPendingInvitations(email.toLowerCase(Locale.ROOT), clock.instant());
    }

    @Transactional
    public void respond(UUID userId, String email, UUID invitationId, boolean accept) {
        var invitation = spaces.lockInvitation(invitationId, email.toLowerCase(Locale.ROOT))
                .orElseThrow(PlanningSpaceService::notFound);
        var now = clock.instant();
        if (!"PENDING".equals(invitation.status()) || !invitation.expiresAt().isAfter(now)) {
            throw bad("INVITATION_EXPIRED", "Este convite não está mais disponível.");
        }
        if (accept) {
            if (spaces.activeMemberCount(invitation.spaceId()) >= beta.maximumSharedMembers()) {
                throw bad("SHARED_SPACE_MEMBER_LIMIT", "Este espaço já atingiu o limite de participantes do beta.");
            }
            spaces.acceptInvitation(invitationId, invitation.spaceId(), userId, invitation.role(), now);
            audit.record(userId, "SPACE_INVITATION_ACCEPTED", "PLANNING_SPACE", invitation.spaceId(), "SUCCESS", now);
        } else {
            spaces.rejectInvitation(invitationId, now);
            audit.record(userId, "SPACE_INVITATION_REJECTED", "PLANNING_SPACE", invitation.spaceId(), "SUCCESS", now);
        }
    }

    public List<PlanningSpaceRepository.Member> members(UUID userId, UUID spaceId) {
        return spaces.listMembers(userId, spaceId);
    }

    @Transactional
    public void changeRole(UUID actorId, UUID spaceId, UUID memberId, String role) {
        requireOwner(actorId, spaceId);
        String normalizedRole = normalizeRole(role);
        String current = spaces.memberRole(spaceId, memberId).orElseThrow(PlanningSpaceService::notFound);
        if ("OWNER".equals(current) && !"OWNER".equals(normalizedRole) && spaces.ownerCount(spaceId) <= 1) {
            throw bad("LAST_OWNER_REQUIRED", "O espaço precisa manter ao menos um proprietário.");
        }
        spaces.updateMemberRole(spaceId, memberId, normalizedRole);
        audit.record(actorId, "SPACE_MEMBER_ROLE_CHANGED", "PLANNING_SPACE", spaceId, "SUCCESS", clock.instant());
    }

    public PlanningSpaceRepository.SharedProfile profile(UUID userId, UUID spaceId) {
        return spaces.findProfile(userId, spaceId).orElseThrow(PlanningSpaceService::notFound);
    }

    @Transactional
    public PlanningSpaceRepository.SharedProfile saveProfile(UUID userId, UUID spaceId, BigDecimal income,
                                                              BigDecimal expenses, BigDecimal balance,
                                                              BigDecimal confirmed) {
        if (!spaces.canEdit(userId, spaceId)) throw notFound();
        BigDecimal normalizedIncome = money(income);
        BigDecimal normalizedExpenses = money(expenses);
        var profile = new PlanningSpaceRepository.SharedProfile(spaceId, normalizedIncome, normalizedExpenses,
                money(balance), normalizedIncome.subtract(normalizedExpenses).max(BigDecimal.ZERO).setScale(2),
                money(confirmed), product.defaultCurrency(),
                LocalDate.now(clock.withZone(product.businessTimeZone())));
        var now = clock.instant();
        spaces.saveProfile(profile, now);
        audit.record(userId, "SHARED_FINANCIAL_PROFILE_SAVED", "PLANNING_SPACE", spaceId, "SUCCESS", now);
        return profile;
    }

    private void requireOwner(UUID userId, UUID spaceId) {
        if (!spaces.isOwner(userId, spaceId)) throw notFound();
    }

    private static BigDecimal money(BigDecimal value) {
        if (value == null || value.signum() < 0 || value.scale() > 2 || value.precision() - value.scale() > 17) {
            throw bad("INVALID_MONEY_VALUE", "Use um valor não negativo com no máximo duas casas decimais.");
        }
        return value.setScale(2, RoundingMode.UNNECESSARY);
    }

    private static String normalizeEmail(String value) {
        String email = value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
        if (email.length() > 320 || !email.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
            throw bad("INVALID_EMAIL", "Informe um e-mail válido.");
        }
        return email;
    }

    private static String normalizeRole(String role) {
        String normalized = role == null ? "" : role.trim().toUpperCase(Locale.ROOT);
        if (!ROLES.contains(normalized)) throw bad("INVALID_SPACE_ROLE", "Escolha um papel disponível.");
        return normalized;
    }

    private static String text(String value, int min, int max, String code, String message) {
        String normalized = value == null ? "" : value.trim().replaceAll("\\s+", " ");
        if (normalized.length() < min || normalized.length() > max) throw bad(code, message);
        return normalized;
    }

    private static IdentityException notFound() {
        return new IdentityException(IdentityException.Kind.NOT_FOUND, "PLANNING_SPACE_NOT_FOUND", "Espaço não encontrado.");
    }

    private static IdentityException bad(String code, String message) {
        return new IdentityException(IdentityException.Kind.BAD_REQUEST, code, message);
    }
}
