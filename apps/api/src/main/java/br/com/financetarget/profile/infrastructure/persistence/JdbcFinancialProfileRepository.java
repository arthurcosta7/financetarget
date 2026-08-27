package br.com.financetarget.profile.infrastructure.persistence;

import br.com.financetarget.profile.application.FinancialProfileRepository;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Repository
public class JdbcFinancialProfileRepository implements FinancialProfileRepository {
    private final JdbcClient jdbc;

    public JdbcFinancialProfileRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public Optional<Profile> findPersonalProfile(UUID userId) {
        return jdbc.sql("""
                        select p.* from financial_profile p
                        join planning_space s on s.id=p.space_id and s.type='PERSONAL'
                        join space_member m on m.space_id=s.id
                        where m.user_id=:userId and m.role='OWNER' and m.status='ACTIVE'
                        """).param("userId", userId)
                .query((rs, row) -> new Profile(rs.getObject("space_id", UUID.class),
                        rs.getBigDecimal("recurring_income"), rs.getBigDecimal("essential_expenses"),
                        rs.getBigDecimal("initial_goal_balance"), rs.getBigDecimal("suggested_monthly_capacity"),
                        rs.getBigDecimal("confirmed_monthly_capacity"), rs.getString("currency"),
                        rs.getDate("reference_date").toLocalDate())).optional();
    }

    @Override
    public UUID requirePersonalSpace(UUID userId) {
        return jdbc.sql("""
                        select s.id from planning_space s join space_member m on m.space_id=s.id
                        where s.type='PERSONAL' and s.status='ACTIVE' and m.user_id=:userId
                          and m.role='OWNER' and m.status='ACTIVE'
                        """).param("userId", userId).query(UUID.class).single();
    }

    @Override
    public void save(Profile profile, Instant now) {
        jdbc.sql("""
                        insert into financial_profile(space_id,recurring_income,essential_expenses,initial_goal_balance,
                            suggested_monthly_capacity,confirmed_monthly_capacity,currency,reference_date,updated_at)
                        values (:spaceId,:income,:expenses,:balance,:suggested,:confirmed,:currency,:referenceDate,:now)
                        on conflict (space_id) do update set recurring_income=excluded.recurring_income,
                            essential_expenses=excluded.essential_expenses,initial_goal_balance=excluded.initial_goal_balance,
                            suggested_monthly_capacity=excluded.suggested_monthly_capacity,
                            confirmed_monthly_capacity=excluded.confirmed_monthly_capacity,currency=excluded.currency,
                            reference_date=excluded.reference_date,version=financial_profile.version+1,updated_at=excluded.updated_at
                        """).param("spaceId", profile.spaceId()).param("income", profile.recurringIncome())
                .param("expenses", profile.essentialExpenses()).param("balance", profile.initialGoalBalance())
                .param("suggested", profile.suggestedMonthlyCapacity()).param("confirmed", profile.confirmedMonthlyCapacity())
                .param("currency", profile.currency()).param("referenceDate", profile.referenceDate()).param("now", dbTime(now)).update();
    }

    @Override
    public void recordConsent(UUID userId, String purpose, String documentVersion, Instant now) {
        jdbc.sql("""
                        insert into consent_record(id,user_id,purpose,document_version,decision,source,recorded_at)
                        values (:id,:userId,:purpose,:version,'GRANTED','ONBOARDING',:now)
                        """).param("id", UUID.randomUUID()).param("userId", userId).param("purpose", purpose)
                .param("version", documentVersion).param("now", dbTime(now)).update();
    }

    private static java.time.OffsetDateTime dbTime(Instant value) {
        return java.time.OffsetDateTime.ofInstant(value, java.time.ZoneOffset.UTC);
    }
}
