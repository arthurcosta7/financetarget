package br.com.financetarget.system.application;

import br.com.financetarget.system.domain.SystemStatus;
import org.springframework.boot.info.BuildProperties;
import org.springframework.stereotype.Service;

@Service
public class SystemStatusService {

    private final SystemMetadataReader metadataReader;
    private final BuildProperties buildProperties;

    public SystemStatusService(SystemMetadataReader metadataReader, BuildProperties buildProperties) {
        this.metadataReader = metadataReader;
        this.buildProperties = buildProperties;
    }

    public SystemStatus currentStatus() {
        return new SystemStatus(
                "UP",
                buildProperties.get("revision"),
                new SystemStatus.DatabaseStatus("UP", metadataReader.schemaVersion())
        );
    }
}
