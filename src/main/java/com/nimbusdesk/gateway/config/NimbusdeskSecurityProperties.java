package com.nimbusdesk.gateway.config;

import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Binds the {@code nimbusdesk.*} configuration block: the shared JWT secret used to
 * validate tokens issued by nimbusdesk-identity-service, and the list of routes that
 * do not require authentication.
 */
@Component
@ConfigurationProperties(prefix = "nimbusdesk")
public class NimbusdeskSecurityProperties {

    private Security security = new Security();
    private List<String> publicEndpoints = List.of();

    public Security getSecurity() {
        return security;
    }

    public void setSecurity(Security security) {
        this.security = security;
    }

    public List<String> getPublicEndpoints() {
        return publicEndpoints;
    }

    public void setPublicEndpoints(List<String> publicEndpoints) {
        this.publicEndpoints = publicEndpoints;
    }

    public static class Security {
        private String jwtSecret;

        public String getJwtSecret() {
            return jwtSecret;
        }

        public void setJwtSecret(String jwtSecret) {
            this.jwtSecret = jwtSecret;
        }
    }
}
