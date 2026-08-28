package br.com.financetarget.subscriptions.application;

public class SubscriptionException extends RuntimeException {
    public enum Kind { BAD_REQUEST, NOT_FOUND, CONFLICT, UNAUTHORIZED, DISABLED }

    private final Kind kind;
    private final String code;

    public SubscriptionException(Kind kind, String code, String message) {
        super(message);
        this.kind = kind;
        this.code = code;
    }

    public Kind kind() { return kind; }
    public String code() { return code; }
}
