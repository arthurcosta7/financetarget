package br.com.financetarget.goals.application;

public class GoalException extends RuntimeException {
    public enum Kind { BAD_REQUEST, NOT_FOUND, CONFLICT }

    private final Kind kind;
    private final String code;

    public GoalException(Kind kind, String code, String message) {
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
