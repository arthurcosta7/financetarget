package br.com.financetarget.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("app.legal-documents")
public record LegalDocumentProperties(String termsVersion, String privacyNoticeVersion) {
}
