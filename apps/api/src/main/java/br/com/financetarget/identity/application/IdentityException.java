package br.com.financetarget.identity.application;

public class IdentityException extends RuntimeException {
    public enum Kind { BAD_REQUEST, UNAUTHORIZED, NOT_FOUND, TOO_MANY_REQUESTS }

    private final Kind kind;
    private final String code;

    public IdentityException(Kind kind, String code, String message) {
        super(message);
        this.kind = kind;
        this.code = code;
    }

    public Kind kind() {
        return kind;
    }

    public String code() {
        return code;
    }
}
