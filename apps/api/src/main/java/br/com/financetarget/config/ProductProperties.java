package br.com.financetarget.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("app.product")
public record ProductProperties(String defaultCurrency, java.time.ZoneId businessTimeZone) {
    public ProductProperties {
        if (defaultCurrency == null || !defaultCurrency.matches("^[A-Z]{3}$")) {
            throw new IllegalArgumentException("app.product.default-currency deve usar código ISO 4217 de três letras");
        }
        if (businessTimeZone == null) {
            throw new IllegalArgumentException("app.product.business-time-zone é obrigatório");
        }
    }
}
