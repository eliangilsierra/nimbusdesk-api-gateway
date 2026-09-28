package com.nimbusdesk.gateway.security;

import com.nimbusdesk.gateway.config.NimbusdeskSecurityProperties;
import com.nimbusdesk.gateway.exception.UnauthorizedException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Component;

/**
 * Validates JWTs signed by nimbusdesk-identity-service with the shared secret
 * configured under {@code nimbusdesk.security.jwt-secret}. The gateway never issues
 * tokens itself — it only verifies them before letting a request through.
 */
@Component
public class JwtProvider {

    private final SecretKey signingKey;

    public JwtProvider(NimbusdeskSecurityProperties properties) {
        this.signingKey = Keys.hmacShaKeyFor(
                properties.getSecurity().getJwtSecret().getBytes(StandardCharsets.UTF_8));
    }

    public Claims validateAndExtractClaims(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(signingKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (JwtException | IllegalArgumentException e) {
            throw new UnauthorizedException("Invalid or expired token");
        }
    }
}
