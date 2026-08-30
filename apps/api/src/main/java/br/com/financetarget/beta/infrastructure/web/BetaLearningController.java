package br.com.financetarget.beta.infrastructure.web;

import br.com.financetarget.beta.application.BetaLearningService;
import br.com.financetarget.identity.application.AuthenticatedAccount;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/beta")
public class BetaLearningController {
    private final BetaLearningService beta;
    public BetaLearningController(BetaLearningService beta) { this.beta = beta; }

    @GetMapping("/config")
    ResponseEntity<BetaLearningService.Config> config() {
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(beta.config());
    }

    @PostMapping("/events")
    ResponseEntity<Void> event(@AuthenticationPrincipal AuthenticatedAccount account,
                               @Valid @RequestBody EventRequest request) {
        beta.event(account.userId(), request.eventName(), request.journeyStage(), request.outcome(), request.deviceClass());
        return ResponseEntity.accepted().cacheControl(CacheControl.noStore()).build();
    }

    @PostMapping("/feedback")
    ResponseEntity<BetaLearningService.Feedback> feedback(@AuthenticationPrincipal AuthenticatedAccount account,
                                                          @Valid @RequestBody FeedbackRequest request) {
        return ResponseEntity.status(201).cacheControl(CacheControl.noStore())
                .body(beta.feedback(account.userId(), request.category(), request.rating(), request.comment()));
    }

    public record EventRequest(@NotBlank @Size(max = 48) String eventName,
                               @NotBlank @Size(max = 32) String journeyStage,
                               @NotBlank @Size(max = 24) String outcome,
                               @NotBlank @Size(max = 16) String deviceClass) {
        @JsonAnySetter
        public void rejectUnknownProperty(String name, Object value) {
            throw new IllegalArgumentException("Propriedade não permitida na telemetria do beta: " + name);
        }
    }
    public record FeedbackRequest(@NotBlank @Size(max = 32) String category,
                                  @Min(1) @Max(5) Integer rating,
                                  @Size(max = 500) String comment) {
        @JsonAnySetter
        public void rejectUnknownProperty(String name, Object value) {
            throw new IllegalArgumentException("Propriedade não permitida no feedback do beta: " + name);
        }
    }
}
