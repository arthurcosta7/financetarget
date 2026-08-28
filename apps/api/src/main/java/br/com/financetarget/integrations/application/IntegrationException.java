package br.com.financetarget.integrations.application;

public class IntegrationException extends RuntimeException {
    public enum Kind { TRANSIENT, PERMANENT, AUTHENTICATION, RATE_LIMIT, DISABLED }

    private final Kind kind;

    public IntegrationException(Kind kind, String message) {
        super(message);
        this.kind = kind;
    }

    public Kind kind() { return kind; }
}
