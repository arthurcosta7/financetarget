package br.com.financetarget.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("app.features")
public record FeatureFlagProperties(boolean paymentsMock, boolean notificationsMock,
                                    boolean openFinance, boolean loyalty, boolean travel,
                                    boolean realEstateFinancing, boolean autoFinancing) {
}
