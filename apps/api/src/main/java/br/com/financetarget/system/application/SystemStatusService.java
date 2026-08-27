package br.com.financetarget.system.application;

import br.com.financetarget.system.domain.SystemStatus;
import org.springframework.stereotype.Service;

@Service
public class SystemStatusService {

    private final SystemMetadataReader metadataReader;

    public SystemStatusService(SystemMetadataReader metadataReader) {
        this.metadataReader = metadataReader;
    }

    public SystemStatus currentStatus() {
        return new SystemStatus(
                "UP",
                new SystemStatus.DatabaseStatus("UP", metadataReader.schemaVersion())
        );
    }
}
