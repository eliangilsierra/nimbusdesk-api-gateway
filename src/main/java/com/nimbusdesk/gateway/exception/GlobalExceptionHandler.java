package com.nimbusdesk.gateway.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Converts gateway-level exceptions into a consistent JSON error body. Spring Cloud
 * Gateway runs on WebFlux, so a regular {@code @RestControllerAdvice} (MVC-only)
 * would never see exceptions thrown from a {@code GlobalFilter} — this is the
 * reactive equivalent.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class GlobalExceptionHandler implements ErrorWebExceptionHandler {

    private final ObjectMapper objectMapper;

    public GlobalExceptionHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        HttpStatus status = resolveStatus(ex);

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", Instant.now().toString());
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("message", ex.getMessage());
        body.put("path", exchange.getRequest().getURI().getPath());

        exchange.getResponse().setStatusCode(status);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);

        byte[] bytes;
        try {
            bytes = objectMapper.writeValueAsBytes(body);
        } catch (Exception serializationError) {
            bytes = ("{\"message\":\"" + status.getReasonPhrase() + "\"}").getBytes(StandardCharsets.UTF_8);
        }

        return exchange.getResponse()
                .writeWith(Mono.just(exchange.getResponse().bufferFactory().wrap(bytes)));
    }

    private HttpStatus resolveStatus(Throwable ex) {
        if (ex instanceof UnauthorizedException) {
            return HttpStatus.UNAUTHORIZED;
        }
        if (ex instanceof BadRequestException) {
            return HttpStatus.BAD_REQUEST;
        }
        if (ex instanceof InternalServerException) {
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }
        return HttpStatus.INTERNAL_SERVER_ERROR;
    }
}
