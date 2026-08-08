package com.mandateguardian.shared.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "mandate-guardian.local-security")
public record LocalSecurityProperties(String username, String password) {
}
