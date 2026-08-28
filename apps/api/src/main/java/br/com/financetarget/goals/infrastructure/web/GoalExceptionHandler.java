package br.com.financetarget.goals.infrastructure.web;

import br.com.financetarget.goals.application.GoalException;
import br.com.financetarget.planning.domain.ProjectionException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GoalExceptionHandler {
    @ExceptionHandler(GoalException.class)
    ResponseEntity<ProblemDetail> goal(GoalException exception) {
        HttpStatus status = switch (exception.kind()) {
            case BAD_REQUEST -> HttpStatus.BAD_REQUEST;
            case NOT_FOUND -> HttpStatus.NOT_FOUND;
            case CONFLICT -> HttpStatus.CONFLICT;
        };
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, exception.getMessage());
        problem.setTitle(exception.code());
        return ResponseEntity.status(status).body(problem);
    }

    @ExceptionHandler(ProjectionException.class)
    ResponseEntity<ProblemDetail> projection(ProjectionException exception) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, exception.getMessage());
        problem.setTitle(exception.code());
        return ResponseEntity.badRequest().body(problem);
    }
}
