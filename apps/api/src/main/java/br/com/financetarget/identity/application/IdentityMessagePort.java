package br.com.financetarget.identity.application;

public interface IdentityMessagePort {
    void sendVerification(String normalizedEmail, String token);

    void sendPasswordRecovery(String normalizedEmail, String token);
}
