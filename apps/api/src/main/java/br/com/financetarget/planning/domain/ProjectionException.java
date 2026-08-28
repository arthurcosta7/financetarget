package br.com.financetarget.planning.domain;

public class ProjectionException extends RuntimeException {
    private final String code;

    public ProjectionException(String code, String message) {
        super(message);
        this.code = code;
    }

    public String code() {
        return code;
    }
}
