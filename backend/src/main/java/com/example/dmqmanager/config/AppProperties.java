package com.example.dmqmanager.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app")
public record AppProperties(Security security, Solace solace) {
    public record Security(String username, String password) {}

    public record Solace(
            String host,
            String vpn,
            String clientUsername,
            String clientPassword,
            String sempUrl,
            String sempUsername,
            String sempPassword
    ) {}
}
