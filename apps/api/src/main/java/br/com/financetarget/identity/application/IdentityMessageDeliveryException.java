package br.com.financetarget.identity.application;

public class IdentityMessageDeliveryException extends RuntimeException {
    public IdentityMessageDeliveryException(Throwable cause) {
        super("Não foi possível entregar a mensagem de identidade.", cause);
    }
}
