package com.nimbusdesk.gateway.filter;

import com.nimbusdesk.gateway.exception.UnauthorizedException;
import com.nimbusdesk.gateway.security.JwtProvider;
import io.jsonwebtoken.Claims;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import reactor.core.publisher.Mono;

/**
 * Rejects unauthenticated requests to secured routes before they reach any
 * downstream service. Public routes (see {@link RouteValidator}) skip validation
 * entirely — the services behind the gateway never see raw credentials, only
 * already-validated requests.
 */
@Component
public class AuthenticationFilter implements GlobalFilter, Ordered {

    private final RouteValidator routeValidator;
    private final JwtProvider jwtProvider;

    public AuthenticationFilter(RouteValidator routeValidator, JwtProvider jwtProvider) {
        this.routeValidator = routeValidator;
        this.jwtProvider = jwtProvider;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();

        if (!routeValidator.isSecured(request)) {
            return chain.filter(exchange);
        }

        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new UnauthorizedException("Missing bearer token");
        }

        String token = authHeader.substring("Bearer ".length());
        Claims claims = jwtProvider.validateAndExtractClaims(token);

        ServerHttpRequest mutatedRequest = request.mutate()
                .header("X-Auth-User", claims.getSubject())
                .build();

        return chain.filter(exchange.mutate().request(mutatedRequest).build());
    }

    @Override
    public int getOrder() {
        return -1;
    }
}
