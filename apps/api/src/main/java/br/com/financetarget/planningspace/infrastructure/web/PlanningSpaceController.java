package br.com.financetarget.planningspace.infrastructure.web;

import br.com.financetarget.identity.application.AuthenticatedAccount;
import br.com.financetarget.planningspace.application.PlanningSpaceRepository;
import br.com.financetarget.planningspace.application.PlanningSpaceService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
public class PlanningSpaceController {
    private final PlanningSpaceService spaces;

    public PlanningSpaceController(PlanningSpaceService spaces) { this.spaces = spaces; }

    @GetMapping("/planning-spaces")
    ResponseEntity<List<SpaceResponse>> list(@AuthenticationPrincipal AuthenticatedAccount account) {
        return ResponseEntity.ok().cacheControl(CacheControl.noStore())
                .body(spaces.list(account.userId()).stream().map(SpaceResponse::from).toList());
    }

    @PostMapping("/planning-spaces")
    ResponseEntity<SpaceResponse> create(@AuthenticationPrincipal AuthenticatedAccount account,
                                         @Valid @RequestBody CreateSpaceRequest request) {
        var created = spaces.createShared(account.userId(), request.name());
        return ResponseEntity.created(URI.create("/api/v1/planning-spaces/" + created.id()))
                .cacheControl(CacheControl.noStore()).body(SpaceResponse.from(created));
    }

    @PostMapping("/planning-spaces/{spaceId}/invitations")
    ResponseEntity<InvitationResponse> invite(@AuthenticationPrincipal AuthenticatedAccount account,
                                              @PathVariable UUID spaceId,
                                              @Valid @RequestBody CreateInvitationRequest request) {
        var invitation = spaces.invite(account.userId(), account.email(), spaceId, request.email(), request.role());
        return ResponseEntity.accepted().cacheControl(CacheControl.noStore()).body(InvitationResponse.from(invitation));
    }

    @GetMapping("/planning-space-invitations")
    ResponseEntity<List<InvitationResponse>> invitations(@AuthenticationPrincipal AuthenticatedAccount account) {
        return ResponseEntity.ok().cacheControl(CacheControl.noStore())
                .body(spaces.invitations(account.email()).stream().map(InvitationResponse::from).toList());
    }

    @PostMapping("/planning-space-invitations/{invitationId}/responses")
    ResponseEntity<Void> respond(@AuthenticationPrincipal AuthenticatedAccount account,
                                 @PathVariable UUID invitationId,
                                 @Valid @RequestBody InvitationDecisionRequest request) {
        spaces.respond(account.userId(), account.email(), invitationId, request.accept());
        return ResponseEntity.noContent().cacheControl(CacheControl.noStore()).build();
    }

    @GetMapping("/planning-spaces/{spaceId}/members")
    ResponseEntity<List<MemberResponse>> members(@AuthenticationPrincipal AuthenticatedAccount account,
                                                 @PathVariable UUID spaceId) {
        var members = spaces.members(account.userId(), spaceId);
        if (members.isEmpty()) return ResponseEntity.notFound().build();
        return ResponseEntity.ok().cacheControl(CacheControl.noStore())
                .body(members.stream().map(MemberResponse::from).toList());
    }

    @PatchMapping("/planning-spaces/{spaceId}/members/{memberId}")
    ResponseEntity<Void> role(@AuthenticationPrincipal AuthenticatedAccount account, @PathVariable UUID spaceId,
                              @PathVariable UUID memberId, @Valid @RequestBody ChangeRoleRequest request) {
        spaces.changeRole(account.userId(), spaceId, memberId, request.role());
        return ResponseEntity.noContent().cacheControl(CacheControl.noStore()).build();
    }

    @GetMapping("/planning-spaces/{spaceId}/financial-profile")
    ResponseEntity<ProfileResponse> profile(@AuthenticationPrincipal AuthenticatedAccount account,
                                            @PathVariable UUID spaceId) {
        return ResponseEntity.ok().cacheControl(CacheControl.noStore())
                .body(ProfileResponse.from(spaces.profile(account.userId(), spaceId)));
    }

    @PutMapping("/planning-spaces/{spaceId}/financial-profile")
    ResponseEntity<ProfileResponse> saveProfile(@AuthenticationPrincipal AuthenticatedAccount account,
                                                @PathVariable UUID spaceId,
                                                @Valid @RequestBody SaveProfileRequest request) {
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(ProfileResponse.from(
                spaces.saveProfile(account.userId(), spaceId, request.recurringIncome(), request.essentialExpenses(),
                        request.initialGoalBalance(), request.confirmedMonthlyCapacity())));
    }

    public record CreateSpaceRequest(@NotBlank @Size(min = 2, max = 80) String name) {}
    public record CreateInvitationRequest(@NotBlank @Email @Size(max = 320) String email,
                                          @NotBlank @Pattern(regexp = "OWNER|EDITOR|VIEWER") String role) {}
    public record InvitationDecisionRequest(boolean accept) {}
    public record ChangeRoleRequest(@NotBlank @Pattern(regexp = "OWNER|EDITOR|VIEWER") String role) {}
    public record SaveProfileRequest(
            @NotNull @DecimalMin("0.00") @Digits(integer = 17, fraction = 2) BigDecimal recurringIncome,
            @NotNull @DecimalMin("0.00") @Digits(integer = 17, fraction = 2) BigDecimal essentialExpenses,
            @NotNull @DecimalMin("0.00") @Digits(integer = 17, fraction = 2) BigDecimal initialGoalBalance,
            @NotNull @DecimalMin("0.00") @Digits(integer = 17, fraction = 2) BigDecimal confirmedMonthlyCapacity) {}

    public record SpaceResponse(String id, String type, String name, String baseCurrency, String role,
                                int memberCount, boolean profileConfigured) {
        static SpaceResponse from(PlanningSpaceRepository.Space value) {
            return new SpaceResponse(value.id().toString(), value.type(), value.name(), value.baseCurrency(),
                    value.role(), value.memberCount(), value.profileConfigured());
        }
    }
    public record InvitationResponse(String id, String spaceId, String spaceName, String inviterName, String role,
                                     String status, String expiresAt, String createdAt) {
        static InvitationResponse from(PlanningSpaceRepository.Invitation value) {
            return new InvitationResponse(value.id().toString(), value.spaceId().toString(), value.spaceName(),
                    value.inviterName(), value.role(), value.status(), value.expiresAt().toString(),
                    value.createdAt().toString());
        }
    }
    public record MemberResponse(String userId, String displayName, String role, String joinedAt) {
        static MemberResponse from(PlanningSpaceRepository.Member value) {
            return new MemberResponse(value.userId().toString(), value.displayName(), value.role(), value.joinedAt().toString());
        }
    }
    public record ProfileResponse(String spaceId, String recurringIncome, String essentialExpenses,
                                  String initialGoalBalance, String suggestedMonthlyCapacity,
                                  String confirmedMonthlyCapacity, String currency, String referenceDate) {
        static ProfileResponse from(PlanningSpaceRepository.SharedProfile value) {
            return new ProfileResponse(value.spaceId().toString(), value.recurringIncome().toPlainString(),
                    value.essentialExpenses().toPlainString(), value.initialGoalBalance().toPlainString(),
                    value.suggestedMonthlyCapacity().toPlainString(), value.confirmedMonthlyCapacity().toPlainString(),
                    value.currency(), value.referenceDate().toString());
        }
    }
}
