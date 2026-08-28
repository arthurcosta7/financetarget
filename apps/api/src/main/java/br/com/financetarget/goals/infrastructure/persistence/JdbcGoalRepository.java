package br.com.financetarget.goals.infrastructure.persistence;

import br.com.financetarget.goals.application.GoalRepository;
import br.com.financetarget.planning.domain.ContributionTiming;
import br.com.financetarget.planning.domain.GoalProjection;
import br.com.financetarget.planning.domain.GoalProjectionInput;
import br.com.financetarget.planning.domain.TargetValueBasis;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class JdbcGoalRepository implements GoalRepository {
    private final JdbcClient jdbc;
    private final ObjectMapper json;

    public JdbcGoalRepository(JdbcClient jdbc, ObjectMapper json) {
        this.jdbc = jdbc;
        this.json = json;
    }

    @Override
    public Optional<SpaceContext> findSpace(UUID userId, UUID spaceId, boolean editable) {
        String roleClause = editable ? "and m.role in ('OWNER','EDITOR')" : "";
        return jdbc.sql("""
                        select s.id,s.base_currency,p.confirmed_monthly_capacity
                        from planning_space s
                        join space_member m on m.space_id=s.id and m.status='ACTIVE'
                        left join financial_profile p on p.space_id=s.id
                        where s.id=:spaceId and s.status='ACTIVE' and m.user_id=:userId
                        """ + roleClause)
                .param("spaceId", spaceId).param("userId", userId)
                .query((rs, row) -> new SpaceContext(rs.getObject("id", UUID.class),
                        rs.getString("base_currency"), rs.getBigDecimal("confirmed_monthly_capacity"))).optional();
    }

    @Override
    public Optional<StoredGoal> findGoal(UUID userId, UUID spaceId, UUID goalId, boolean editable) {
        String roleClause = editable ? "and m.role in ('OWNER','EDITOR')" : "";
        return jdbc.sql("""
                        select g.* from goal g
                        join planning_space s on s.id=g.space_id and s.status='ACTIVE'
                        join space_member m on m.space_id=s.id and m.status='ACTIVE'
                        where g.id=:goalId and g.space_id=:spaceId and m.user_id=:userId
                        """ + roleClause)
                .param("goalId", goalId).param("spaceId", spaceId).param("userId", userId)
                .query(this::mapGoal).optional();
    }

    @Override
    public List<StoredGoal> listGoals(UUID userId, UUID spaceId) {
        return jdbc.sql("""
                        select g.* from goal g
                        join planning_space s on s.id=g.space_id and s.status='ACTIVE'
                        join space_member m on m.space_id=s.id and m.status='ACTIVE'
                        where g.space_id=:spaceId and m.user_id=:userId and g.status='ACTIVE'
                        order by g.created_at,g.id
                        """).param("spaceId", spaceId).param("userId", userId).query(this::mapGoal).list();
    }

    @Override
    public void insertGoal(StoredGoal goal, GoalProjectionInput input, GoalProjection projection,
                           String inputHash, UUID actorUserId, Instant now) {
        var timestamp = dbTime(now);
        jdbc.sql("""
                        insert into goal(id,space_id,created_by,goal_type,title,target_amount,target_value_basis,
                            target_date,initial_balance,annual_inflation_rate,annual_return_rate,contribution_timing,
                            planned_monthly_contribution,currency,status,created_at,updated_at)
                        values (:id,:spaceId,:createdBy,:goalType,:title,:targetAmount,:basis,:targetDate,
                            :initialBalance,:inflation,:returnRate,:timing,:planned,:currency,'ACTIVE',:now,:now)
                        """).param("id", goal.id()).param("spaceId", goal.spaceId()).param("createdBy", actorUserId)
                .param("goalType", goal.goalType()).param("title", goal.title()).param("targetAmount", goal.targetAmount())
                .param("basis", goal.targetValueBasis().name()).param("targetDate", goal.targetDate())
                .param("initialBalance", goal.initialBalance()).param("inflation", goal.annualInflationRate())
                .param("returnRate", goal.annualReturnRate()).param("timing", goal.contributionTiming().name())
                .param("planned", goal.plannedMonthlyContribution()).param("currency", goal.currency())
                .param("now", timestamp).update();
        jdbc.sql("""
                        insert into calculation_snapshot(id,space_id,goal_id,created_by,input_hash,engine_version,
                            formula_version,normalized_inputs,projection_result,warnings,origin,created_at)
                        values (:id,:spaceId,:goalId,:createdBy,:inputHash,:engineVersion,:formulaVersion,
                            cast(:inputs as jsonb),cast(:result as jsonb),cast(:warnings as jsonb),'GOAL_CREATED',:now)
                        """).param("id", UUID.randomUUID()).param("spaceId", goal.spaceId()).param("goalId", goal.id())
                .param("createdBy", actorUserId).param("inputHash", inputHash)
                .param("engineVersion", projection.engineVersion()).param("formulaVersion", projection.formulaVersion())
                .param("inputs", write(input)).param("result", write(projection)).param("warnings", write(projection.warnings()))
                .param("now", timestamp).update();
    }

    @Override
    public GoalProjection latestProjection(UUID goalId) {
        String value = jdbc.sql("""
                        select projection_result::text from calculation_snapshot
                        where goal_id=:goalId order by created_at desc,id desc limit 1
                        """).param("goalId", goalId).query(String.class).single();
        try {
            return json.readValue(value, GoalProjection.class);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Snapshot de projeção inválido.", exception);
        }
    }

    @Override
    public BigDecimal contributionsTotal(UUID goalId) {
        return jdbc.sql("select coalesce(sum(amount),0) from contribution where goal_id=:goalId")
                .param("goalId", goalId).query(BigDecimal.class).single();
    }

    @Override
    public List<Contribution> listContributions(UUID goalId) {
        return jdbc.sql("""
                        select * from contribution where goal_id=:goalId
                        order by contribution_date desc,created_at desc,id desc
                        """).param("goalId", goalId).query((rs, row) -> new Contribution(
                        rs.getObject("id", UUID.class), rs.getObject("goal_id", UUID.class),
                        rs.getObject("created_by", UUID.class), rs.getBigDecimal("amount"), rs.getString("currency"),
                        rs.getDate("contribution_date").toLocalDate(), rs.getString("note"),
                        rs.getString("idempotency_key"), rs.getObject("created_at", OffsetDateTime.class).toInstant())).list();
    }

    @Override
    public Optional<Contribution> findContribution(UUID goalId, UUID actorUserId, String idempotencyKey) {
        return jdbc.sql("""
                        select * from contribution where goal_id=:goalId and created_by=:actor
                          and idempotency_key=:idempotencyKey
                        """).param("goalId", goalId).param("actor", actorUserId).param("idempotencyKey", idempotencyKey)
                .query((rs, row) -> new Contribution(rs.getObject("id", UUID.class),
                        rs.getObject("goal_id", UUID.class), rs.getObject("created_by", UUID.class),
                        rs.getBigDecimal("amount"), rs.getString("currency"),
                        rs.getDate("contribution_date").toLocalDate(), rs.getString("note"),
                        rs.getString("idempotency_key"), rs.getObject("created_at", OffsetDateTime.class).toInstant())).optional();
    }

    @Override
    public void insertContribution(UUID spaceId, Contribution contribution) {
        jdbc.sql("""
                        insert into contribution(id,space_id,goal_id,created_by,amount,currency,contribution_date,
                            note,idempotency_key,created_at)
                        values (:id,:spaceId,:goalId,:createdBy,:amount,:currency,:date,:note,:key,:createdAt)
                        """).param("id", contribution.id()).param("spaceId", spaceId).param("goalId", contribution.goalId())
                .param("createdBy", contribution.createdBy()).param("amount", contribution.amount())
                .param("currency", contribution.currency()).param("date", contribution.contributionDate())
                .param("note", contribution.note()).param("key", contribution.idempotencyKey())
                .param("createdAt", dbTime(contribution.createdAt())).update();
    }

    private StoredGoal mapGoal(java.sql.ResultSet rs, int row) throws java.sql.SQLException {
        return new StoredGoal(rs.getObject("id", UUID.class), rs.getObject("space_id", UUID.class),
                rs.getObject("created_by", UUID.class), rs.getString("goal_type"), rs.getString("title"),
                rs.getBigDecimal("target_amount"), TargetValueBasis.valueOf(rs.getString("target_value_basis")),
                rs.getDate("target_date").toLocalDate(), rs.getBigDecimal("initial_balance"),
                rs.getBigDecimal("annual_inflation_rate"), rs.getBigDecimal("annual_return_rate"),
                ContributionTiming.valueOf(rs.getString("contribution_timing")),
                rs.getBigDecimal("planned_monthly_contribution"), rs.getString("currency"), rs.getString("status"),
                rs.getLong("version"), rs.getObject("created_at", OffsetDateTime.class).toInstant());
    }

    private String write(Object value) {
        try {
            return json.writeValueAsString(value);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Não foi possível serializar o snapshot.", exception);
        }
    }

    private static OffsetDateTime dbTime(Instant value) {
        return OffsetDateTime.ofInstant(value, ZoneOffset.UTC);
    }
}
