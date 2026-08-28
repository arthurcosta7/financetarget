package br.com.financetarget.identity.infrastructure.web;

import br.com.financetarget.identity.application.IdentityException;
import br.com.financetarget.identity.application.IdentityMessageDeliveryException;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler(IdentityMessageDeliveryException.class)
    ResponseEntity<ProblemDetail> messageDelivery(IdentityMessageDeliveryException exception) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE,
                "Não foi possível enviar a mensagem agora. Tente novamente em alguns instantes.");
        problem.setTitle("IDENTITY_MESSAGE_UNAVAILABLE");
        return ResponseEntity.status(org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE).body(problem);
    }

    @ExceptionHandler(IdentityException.class)
    ResponseEntity<ProblemDetail> identity(IdentityException exception) {
        var status = switch (exception.kind()) {
            case BAD_REQUEST -> org.springframework.http.HttpStatus.BAD_REQUEST;
            case UNAUTHORIZED -> org.springframework.http.HttpStatus.UNAUTHORIZED;
            case NOT_FOUND -> org.springframework.http.HttpStatus.NOT_FOUND;
            case TOO_MANY_REQUESTS -> org.springframework.http.HttpStatus.TOO_MANY_REQUESTS;
        };
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, exception.getMessage());
        problem.setTitle(exception.code());
        return ResponseEntity.status(status).body(problem);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ProblemDetail> validation(MethodArgumentNotValidException exception) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(org.springframework.http.HttpStatus.BAD_REQUEST,
                "Revise os campos informados.");
        problem.setTitle("VALIDATION_ERROR");
        return ResponseEntity.badRequest().body(problem);
    }
}
