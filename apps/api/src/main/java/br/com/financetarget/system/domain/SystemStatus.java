package br.com.financetarget.system.domain;

public record SystemStatus(String status, String releaseId, DatabaseStatus database) {

    public record DatabaseStatus(String status, String schemaVersion) {
    }
}
