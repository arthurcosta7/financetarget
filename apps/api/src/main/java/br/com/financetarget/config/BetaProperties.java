package br.com.financetarget.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties("app.beta")
public record BetaProperties(boolean enabled, Duration invitationTtl, int invitationDailyLimit,
                             int maximumSharedMembers, int feedbackCommentMaximumLength) {
    public BetaProperties {
        if (invitationTtl == null || invitationTtl.isNegative() || invitationTtl.isZero()) {
            throw new IllegalArgumentException("app.beta.invitation-ttl deve ser positiva");
        }
        if (invitationDailyLimit < 1 || invitationDailyLimit > 100) {
            throw new IllegalArgumentException("app.beta.invitation-daily-limit deve estar entre 1 e 100");
        }
        if (maximumSharedMembers < 2 || maximumSharedMembers > 10) {
            throw new IllegalArgumentException("app.beta.maximum-shared-members deve estar entre 2 e 10");
        }
        if (feedbackCommentMaximumLength < 100 || feedbackCommentMaximumLength > 2000) {
            throw new IllegalArgumentException("app.beta.feedback-comment-maximum-length deve estar entre 100 e 2000");
        }
    }
}
