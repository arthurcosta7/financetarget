package br.com.financetarget.system.domain;

public record SystemStatus(String status, DatabaseStatus database) {

    public record DatabaseStatus(String status, String schemaVersion) {
    }
}
