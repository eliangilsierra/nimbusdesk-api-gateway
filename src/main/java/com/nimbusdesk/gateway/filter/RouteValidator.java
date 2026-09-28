package com.nimbusdesk.gateway.filter;

import com.nimbusdesk.gateway.config.NimbusdeskSecurityProperties;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;

/**
 * Decides whether an incoming request must carry a valid JWT before being routed
 * downstream. Everything is secured by default except the endpoints explicitly
 * listed under {@code nimbusdesk.public-endpoints}.
 */
@Component
public class RouteValidator {

    private final NimbusdeskSecurityProperties properties;

    public RouteValidator(NimbusdeskSecurityProperties properties) {
        this.properties = properties;
    }

    public boolean isSecured(ServerHttpRequest request) {
        String path = request.getURI().getPath();
        return properties.getPublicEndpoints().stream().noneMatch(path::startsWith);
    }
}
