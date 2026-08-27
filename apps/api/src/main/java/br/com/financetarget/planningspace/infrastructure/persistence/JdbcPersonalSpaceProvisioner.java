package br.com.financetarget.planningspace.infrastructure.persistence;

import br.com.financetarget.planningspace.application.PersonalSpaceProvisioner;
import br.com.financetarget.config.ProductProperties;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

@Repository
public class JdbcPersonalSpaceProvisioner implements PersonalSpaceProvisioner {
    private final JdbcClient jdbc;
    private final ProductProperties product;

    public JdbcPersonalSpaceProvisioner(JdbcClient jdbc, ProductProperties product) {
        this.jdbc = jdbc;
        this.product = product;
    }

    @Override
    public UUID createForVerifiedUser(UUID userId, Instant now) {
        UUID spaceId = UUID.randomUUID();
        OffsetDateTime timestamp = OffsetDateTime.ofInstant(now, ZoneOffset.UTC);
        jdbc.sql("""
                        insert into planning_space(id,type,name,base_currency,status,created_at,updated_at)
                        values (:id,'PERSONAL','Meu planejamento',:currency,'ACTIVE',:now,:now)
                        """).param("id", spaceId).param("currency", product.defaultCurrency()).param("now", timestamp).update();
        jdbc.sql("""
                        insert into space_member(space_id,user_id,role,status,joined_at)
                        values (:spaceId,:userId,'OWNER','ACTIVE',:now)
                        """).param("spaceId", spaceId).param("userId", userId).param("now", timestamp).update();
        return spaceId;
    }
}
