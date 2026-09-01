package br.com.financetarget.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("app.deployment")
public record DeploymentProperties(String expectedReleaseId) {
}
