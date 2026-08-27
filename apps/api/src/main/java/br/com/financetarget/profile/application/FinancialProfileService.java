package br.com.financetarget.profile.application;

import br.com.financetarget.config.LegalDocumentProperties;
import br.com.financetarget.config.ProductProperties;
import br.com.financetarget.audit.application.AuditEventPort;
import br.com.financetarget.identity.application.IdentityException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

@Service
public class FinancialProfileService {
    private final FinancialProfileRepository repository;
    private final LegalDocumentProperties legalDocuments;
    private final Clock clock;
    private final AuditEventPort audit;
    private final ProductProperties product;

    public FinancialProfileService(FinancialProfileRepository repository,
                                   LegalDocumentProperties legalDocuments, Clock clock, AuditEventPort audit,
                                   ProductProperties product) {
        this.repository = repository;
        this.legalDocuments = legalDocuments;
        this.clock = clock;
        this.audit = audit;
        this.product = product;
    }

    public Requirements requirements() {
        return new Requirements(legalDocuments.termsVersion(), legalDocuments.privacyNoticeVersion());
    }

    public Optional<FinancialProfileRepository.Profile> find(UUID userId) {
        return repository.findPersonalProfile(userId);
    }

    @Transactional
    public FinancialProfileRepository.Profile save(UUID userId, BigDecimal income, BigDecimal expenses,
                                                    BigDecimal balance, BigDecimal confirmed,
                                                    boolean termsAccepted, boolean privacyAcknowledged) {
        if (!termsAccepted || !privacyAcknowledged) {
            throw new IdentityException(IdentityException.Kind.BAD_REQUEST, "REQUIRED_DOCUMENTS_NOT_ACKNOWLEDGED",
                    "Confirme os documentos aplicáveis para continuar.");
        }
        BigDecimal normalizedIncome = money(income);
        BigDecimal normalizedExpenses = money(expenses);
        BigDecimal normalizedBalance = money(balance);
        BigDecimal suggested = normalizedIncome.subtract(normalizedExpenses).max(BigDecimal.ZERO).setScale(2);
        BigDecimal normalizedConfirmed = money(confirmed);
        UUID spaceId = repository.requirePersonalSpace(userId);
        var profile = new FinancialProfileRepository.Profile(spaceId, normalizedIncome, normalizedExpenses,
                normalizedBalance, suggested, normalizedConfirmed, product.defaultCurrency(),
                LocalDate.now(clock.withZone(product.businessTimeZone())));
        var now = clock.instant();
        repository.save(profile, now);
        repository.recordConsent(userId, "TERMS_ACCEPTANCE", legalDocuments.termsVersion(), now);
        repository.recordConsent(userId, "PRIVACY_NOTICE_ACKNOWLEDGEMENT", legalDocuments.privacyNoticeVersion(), now);
        audit.record(userId, "FINANCIAL_PROFILE_SAVED", "PLANNING_SPACE", spaceId, "SUCCESS", now);
        return profile;
    }

    private static BigDecimal money(BigDecimal value) {
        if (value == null || value.signum() < 0 || value.precision() - value.scale() > 17 || value.scale() > 2) {
            throw new IdentityException(IdentityException.Kind.BAD_REQUEST, "INVALID_MONEY_VALUE",
                    "Use um valor monetário não negativo com no máximo duas casas decimais.");
        }
        return value.setScale(2, RoundingMode.UNNECESSARY);
    }

    public record Requirements(String termsVersion, String privacyNoticeVersion) {}
}
