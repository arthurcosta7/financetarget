package br.com.financetarget.planningspace.infrastructure.persistence;

import br.com.financetarget.planningspace.application.PlanningSpaceRepository;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class JdbcPlanningSpaceRepository implements PlanningSpaceRepository {
    private final JdbcClient jdbc;

    public JdbcPlanningSpaceRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public List<Space> listSpaces(UUID userId) {
        return jdbc.sql("""
                select s.id,s.type,s.name,s.base_currency,m.role,
                       (select count(*) from space_member x where x.space_id=s.id and x.status='ACTIVE') member_count,
                       exists(select 1 from financial_profile p where p.space_id=s.id
                              and (p.recurring_income > 0 or p.essential_expenses > 0
                                   or p.initial_goal_balance > 0 or p.confirmed_monthly_capacity > 0)) profile_configured
                from planning_space s join space_member m on m.space_id=s.id
                where m.user_id=:userId and m.status='ACTIVE' and s.status='ACTIVE'
                order by case s.type when 'PERSONAL' then 0 else 1 end,s.created_at,s.id
                """).param("userId", userId).query((rs, row) -> new Space(
                rs.getObject("id", UUID.class), rs.getString("type"), rs.getString("name"),
                rs.getString("base_currency"), rs.getString("role"), rs.getInt("member_count"),
                rs.getBoolean("profile_configured"))).list();
    }

    @Override
    public void insertSharedSpace(UUID spaceId, UUID ownerId, String name, String currency, Instant now) {
        var timestamp = dbTime(now);
        jdbc.sql("""
                insert into planning_space(id,type,name,base_currency,status,created_at,updated_at)
                values (:id,'SHARED',:name,:currency,'ACTIVE',:now,:now)
                """).param("id", spaceId).param("name", name).param("currency", currency).param("now", timestamp).update();
        jdbc.sql("""
                insert into space_member(space_id,user_id,role,status,joined_at)
                values (:spaceId,:userId,'OWNER','ACTIVE',:now)
                """).param("spaceId", spaceId).param("userId", ownerId).param("now", timestamp).update();
        jdbc.sql("""
                insert into financial_profile(space_id,recurring_income,essential_expenses,initial_goal_balance,
                    suggested_monthly_capacity,confirmed_monthly_capacity,currency,reference_date,updated_at)
                values (:spaceId,0,0,0,0,0,:currency,current_date,:now)
                """).param("spaceId", spaceId).param("currency", currency).param("now", timestamp).update();
    }

    @Override public boolean isOwner(UUID userId, UUID spaceId) { return hasRole(userId, spaceId, "m.role='OWNER'"); }
    @Override public boolean canEdit(UUID userId, UUID spaceId) { return hasRole(userId, spaceId, "m.role in ('OWNER','EDITOR')"); }

    private boolean hasRole(UUID userId, UUID spaceId, String roleClause) {
        return jdbc.sql("""
                select count(*) from planning_space s join space_member m on m.space_id=s.id
                where s.id=:spaceId and s.type='SHARED' and s.status='ACTIVE'
                  and m.user_id=:userId and m.status='ACTIVE' and
                """ + roleClause).param("spaceId", spaceId).param("userId", userId).query(Integer.class).single() == 1;
    }

    @Override public int activeMemberCount(UUID spaceId) {
        return jdbc.sql("select count(*) from space_member where space_id=:spaceId and status='ACTIVE'")
                .param("spaceId", spaceId).query(Integer.class).single();
    }

    @Override public int invitationCountSince(UUID actorId, Instant since) {
        return jdbc.sql("select count(*) from space_invitation where invited_by=:actor and created_at>=:since")
                .param("actor", actorId).param("since", dbTime(since)).query(Integer.class).single();
    }

    @Override public boolean pendingInvitationExists(UUID spaceId, String email, Instant now) {
        jdbc.sql("update space_invitation set status='EXPIRED' where status='PENDING' and expires_at<=:now")
                .param("now", dbTime(now)).update();
        return jdbc.sql("select count(*) from space_invitation where space_id=:spaceId and email_normalized=:email and status='PENDING'")
                .param("spaceId", spaceId).param("email", email).query(Integer.class).single() > 0;
    }

    @Override public void insertInvitation(Invitation invitation, UUID actorId) {
        jdbc.sql("""
                insert into space_invitation(id,space_id,invited_by,email_normalized,role,status,expires_at,created_at)
                values (:id,:spaceId,:actor,:email,:role,'PENDING',:expiresAt,:createdAt)
                """).param("id", invitation.id()).param("spaceId", invitation.spaceId()).param("actor", actorId)
                .param("email", invitation.recipientEmail()).param("role", invitation.role())
                .param("expiresAt", dbTime(invitation.expiresAt())).param("createdAt", dbTime(invitation.createdAt())).update();
    }

    @Override public List<Invitation> listPendingInvitations(String recipientEmail, Instant now) {
        jdbc.sql("update space_invitation set status='EXPIRED' where status='PENDING' and expires_at<=:now")
                .param("now", dbTime(now)).update();
        return jdbc.sql("""
                select i.*,s.name space_name,u.display_name inviter_name
                from space_invitation i join planning_space s on s.id=i.space_id
                join app_user u on u.id=i.invited_by
                where i.email_normalized=:email and i.status='PENDING' and i.expires_at>:now
                order by i.created_at desc
                """).param("email", recipientEmail).param("now", dbTime(now)).query(this::mapInvitation).list();
    }

    @Override public Optional<Invitation> lockInvitation(UUID invitationId, String recipientEmail) {
        return jdbc.sql("""
                select i.*,s.name space_name,u.display_name inviter_name
                from space_invitation i join planning_space s on s.id=i.space_id
                join app_user u on u.id=i.invited_by
                where i.id=:id and i.email_normalized=:email for update
                """).param("id", invitationId).param("email", recipientEmail).query(this::mapInvitation).optional();
    }

    @Override public void acceptInvitation(UUID invitationId, UUID spaceId, UUID userId, String role, Instant now) {
        var timestamp = dbTime(now);
        jdbc.sql("""
                insert into space_member(space_id,user_id,role,status,joined_at)
                values (:spaceId,:userId,:role,'ACTIVE',:now)
                on conflict (space_id,user_id) do update set role=excluded.role,status='ACTIVE',joined_at=excluded.joined_at
                """).param("spaceId", spaceId).param("userId", userId).param("role", role).param("now", timestamp).update();
        jdbc.sql("update space_invitation set status='ACCEPTED',responded_at=:now where id=:id and status='PENDING'")
                .param("now", timestamp).param("id", invitationId).update();
    }

    @Override public void rejectInvitation(UUID invitationId, Instant now) {
        jdbc.sql("update space_invitation set status='REJECTED',responded_at=:now where id=:id and status='PENDING'")
                .param("now", dbTime(now)).param("id", invitationId).update();
    }

    @Override public List<Member> listMembers(UUID userId, UUID spaceId) {
        if (!hasRole(userId, spaceId, "true")) return List.of();
        return jdbc.sql("""
                select u.id,u.display_name,m.role,m.joined_at from space_member m
                join app_user u on u.id=m.user_id
                where m.space_id=:spaceId and m.status='ACTIVE' order by m.joined_at,m.user_id
                """).param("spaceId", spaceId).query((rs, row) -> new Member(rs.getObject("id", UUID.class),
                rs.getString("display_name"), rs.getString("role"),
                rs.getObject("joined_at", OffsetDateTime.class).toInstant())).list();
    }

    @Override public Optional<String> memberRole(UUID spaceId, UUID memberId) {
        return jdbc.sql("select role from space_member where space_id=:spaceId and user_id=:userId and status='ACTIVE'")
                .param("spaceId", spaceId).param("userId", memberId).query(String.class).optional();
    }

    @Override public int ownerCount(UUID spaceId) {
        return jdbc.sql("select count(*) from space_member where space_id=:spaceId and role='OWNER' and status='ACTIVE'")
                .param("spaceId", spaceId).query(Integer.class).single();
    }

    @Override public void updateMemberRole(UUID spaceId, UUID memberId, String role) {
        jdbc.sql("update space_member set role=:role where space_id=:spaceId and user_id=:userId and status='ACTIVE'")
                .param("role", role).param("spaceId", spaceId).param("userId", memberId).update();
    }

    @Override public Optional<SharedProfile> findProfile(UUID userId, UUID spaceId) {
        return jdbc.sql("""
                select p.* from financial_profile p join planning_space s on s.id=p.space_id and s.status='ACTIVE'
                join space_member m on m.space_id=s.id and m.status='ACTIVE'
                where p.space_id=:spaceId and m.user_id=:userId
                """).param("spaceId", spaceId).param("userId", userId).query((rs, row) -> new SharedProfile(
                rs.getObject("space_id", UUID.class), rs.getBigDecimal("recurring_income"),
                rs.getBigDecimal("essential_expenses"), rs.getBigDecimal("initial_goal_balance"),
                rs.getBigDecimal("suggested_monthly_capacity"), rs.getBigDecimal("confirmed_monthly_capacity"),
                rs.getString("currency"), rs.getDate("reference_date").toLocalDate())).optional();
    }

    @Override public void saveProfile(SharedProfile profile, Instant now) {
        jdbc.sql("""
                update financial_profile set recurring_income=:income,essential_expenses=:expenses,
                  initial_goal_balance=:balance,suggested_monthly_capacity=:suggested,
                  confirmed_monthly_capacity=:confirmed,currency=:currency,reference_date=:referenceDate,
                  version=version+1,updated_at=:now where space_id=:spaceId
                """).param("income", profile.recurringIncome()).param("expenses", profile.essentialExpenses())
                .param("balance", profile.initialGoalBalance()).param("suggested", profile.suggestedMonthlyCapacity())
                .param("confirmed", profile.confirmedMonthlyCapacity()).param("currency", profile.currency())
                .param("referenceDate", profile.referenceDate()).param("now", dbTime(now))
                .param("spaceId", profile.spaceId()).update();
    }

    private Invitation mapInvitation(java.sql.ResultSet rs, int row) throws java.sql.SQLException {
        return new Invitation(rs.getObject("id", UUID.class), rs.getObject("space_id", UUID.class),
                rs.getString("space_name"), rs.getString("inviter_name"), rs.getString("email_normalized"),
                rs.getString("role"), rs.getString("status"), rs.getObject("expires_at", OffsetDateTime.class).toInstant(),
                rs.getObject("created_at", OffsetDateTime.class).toInstant());
    }

    private static OffsetDateTime dbTime(Instant value) { return OffsetDateTime.ofInstant(value, ZoneOffset.UTC); }
}
