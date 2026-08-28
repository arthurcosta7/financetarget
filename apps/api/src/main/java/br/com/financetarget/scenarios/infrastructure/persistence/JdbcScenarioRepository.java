package br.com.financetarget.scenarios.infrastructure.persistence;

import br.com.financetarget.goals.application.GoalRepository;
import br.com.financetarget.planning.domain.*;
import br.com.financetarget.scenarios.application.ScenarioRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class JdbcScenarioRepository implements ScenarioRepository {
    private final JdbcClient jdbc;
    private final ObjectMapper json;

    public JdbcScenarioRepository(JdbcClient jdbc, ObjectMapper json) {
        this.jdbc = jdbc;
        this.json = json;
    }

    @Override
    public Optional<Context> findContext(UUID userId, UUID spaceId, UUID goalId, boolean editable) {
        String roleClause = editable ? "and m.role in ('OWNER','EDITOR')" : "";
        return jdbc.sql("""
                select g.*,p.confirmed_monthly_capacity,
                  (select cs.projection_result::text from calculation_snapshot cs
                   where cs.goal_id=g.id and cs.scenario_id is null order by cs.created_at desc,cs.id desc limit 1) base_projection
                from goal g
                join planning_space s on s.id=g.space_id and s.status='ACTIVE'
                join space_member m on m.space_id=s.id and m.status='ACTIVE'
                left join financial_profile p on p.space_id=s.id
                where g.id=:goalId and g.space_id=:spaceId and m.user_id=:userId
                """ + roleClause).param("goalId", goalId).param("spaceId", spaceId).param("userId", userId)
                .query((rs, row) -> new Context(mapGoal(rs), rs.getBigDecimal("confirmed_monthly_capacity"),
                        readProjection(rs.getString("base_projection")))).optional();
    }

    @Override
    public List<StoredScenario> list(UUID goalId) {
        return jdbc.sql("""
                select s.*,cs.projection_result::text projection_result
                from scenario s join calculation_snapshot cs on cs.scenario_id=s.id
                where s.goal_id=:goalId order by s.created_at,s.id
                """).param("goalId", goalId).query((rs, row) -> new StoredScenario(
                rs.getObject("id", UUID.class), rs.getObject("space_id", UUID.class),
                rs.getObject("goal_id", UUID.class), rs.getObject("created_by", UUID.class), rs.getString("title"),
                rs.getDate("target_date").toLocalDate(), rs.getBigDecimal("annual_inflation_rate"),
                rs.getBigDecimal("annual_return_rate"), ContributionTiming.valueOf(rs.getString("contribution_timing")),
                readProjection(rs.getString("projection_result")),
                rs.getObject("created_at", OffsetDateTime.class).toInstant())).list();
    }

    @Override
    public void insert(StoredScenario scenario, GoalProjectionInput input, String inputHash) {
        var createdAt = OffsetDateTime.ofInstant(scenario.createdAt(), ZoneOffset.UTC);
        jdbc.sql("""
                insert into scenario(id,space_id,goal_id,created_by,title,target_date,annual_inflation_rate,
                  annual_return_rate,contribution_timing,created_at)
                values (:id,:spaceId,:goalId,:createdBy,:title,:targetDate,:inflation,:returnRate,:timing,:createdAt)
                """).param("id", scenario.id()).param("spaceId", scenario.spaceId()).param("goalId", scenario.goalId())
                .param("createdBy", scenario.createdBy()).param("title", scenario.title())
                .param("targetDate", scenario.targetDate()).param("inflation", scenario.annualInflationRate())
                .param("returnRate", scenario.annualReturnRate()).param("timing", scenario.contributionTiming().name())
                .param("createdAt", createdAt).update();
        jdbc.sql("""
                insert into calculation_snapshot(id,space_id,goal_id,scenario_id,created_by,input_hash,engine_version,
                  formula_version,normalized_inputs,projection_result,warnings,origin,created_at)
                values (:id,:spaceId,:goalId,:scenarioId,:createdBy,:inputHash,:engineVersion,:formulaVersion,
                  cast(:inputs as jsonb),cast(:result as jsonb),cast(:warnings as jsonb),'SCENARIO_CREATED',:createdAt)
                """).param("id", UUID.randomUUID()).param("spaceId", scenario.spaceId())
                .param("goalId", scenario.goalId()).param("scenarioId", scenario.id())
                .param("createdBy", scenario.createdBy()).param("inputHash", inputHash)
                .param("engineVersion", scenario.projection().engineVersion())
                .param("formulaVersion", scenario.projection().formulaVersion()).param("inputs", write(input))
                .param("result", write(scenario.projection())).param("warnings", write(scenario.projection().warnings()))
                .param("createdAt", createdAt).update();
    }

    private GoalRepository.StoredGoal mapGoal(java.sql.ResultSet rs) throws java.sql.SQLException {
        return new GoalRepository.StoredGoal(rs.getObject("id", UUID.class), rs.getObject("space_id", UUID.class),
                rs.getObject("created_by", UUID.class), rs.getString("goal_type"), rs.getString("title"),
                rs.getBigDecimal("target_amount"), TargetValueBasis.valueOf(rs.getString("target_value_basis")),
                rs.getDate("target_date").toLocalDate(), rs.getBigDecimal("initial_balance"),
                rs.getBigDecimal("annual_inflation_rate"), rs.getBigDecimal("annual_return_rate"),
                ContributionTiming.valueOf(rs.getString("contribution_timing")),
                rs.getBigDecimal("planned_monthly_contribution"), rs.getString("currency"), rs.getString("status"),
                rs.getLong("version"), rs.getObject("created_at", OffsetDateTime.class).toInstant());
    }

    private GoalProjection readProjection(String value) {
        try { return json.readValue(value, GoalProjection.class); }
        catch (JsonProcessingException exception) { throw new IllegalStateException("Snapshot inválido.", exception); }
    }

    private String write(Object value) {
        try { return json.writeValueAsString(value); }
        catch (JsonProcessingException exception) { throw new IllegalStateException("Snapshot inválido.", exception); }
    }
}
