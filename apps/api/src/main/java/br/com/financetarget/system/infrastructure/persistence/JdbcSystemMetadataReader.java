package br.com.financetarget.system.infrastructure.persistence;

import br.com.financetarget.system.application.SystemMetadataReader;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
public class JdbcSystemMetadataReader implements SystemMetadataReader {

    private static final String SCHEMA_VERSION_KEY = "schema_version";

    private final JdbcClient jdbcClient;

    public JdbcSystemMetadataReader(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    @Override
    public String schemaVersion() {
        return jdbcClient.sql("""
                        select metadata_value
                        from app_metadata
                        where metadata_key = :metadataKey
                        """)
                .param("metadataKey", SCHEMA_VERSION_KEY)
                .query(String.class)
                .single();
    }
}
