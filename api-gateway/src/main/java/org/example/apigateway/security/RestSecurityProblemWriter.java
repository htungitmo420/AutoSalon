package org.example.apigateway.security;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.example.apigateway.tracing.TraceIdWebFilter;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.server.ServerAuthenticationEntryPoint;
import org.springframework.security.web.server.authorization.ServerAccessDeniedHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class RestSecurityProblemWriter implements ServerAuthenticationEntryPoint, ServerAccessDeniedHandler {

    private final ObjectMapper objectMapper;

    @Override
    public Mono<Void> commence(ServerWebExchange exchange, AuthenticationException exception) {
        return write(exchange, HttpStatus.UNAUTHORIZED, "UNAUTHENTICATED", "Authentication is required");
    }

    @Override
    public Mono<Void> handle(ServerWebExchange exchange,
                             org.springframework.security.access.AccessDeniedException exception) {
        return write(exchange, HttpStatus.FORBIDDEN, "ACCESS_DENIED", "Access denied");
    }

    private Mono<Void> write(ServerWebExchange exchange, HttpStatus status, String code, String message) {
        exchange.getResponse().setStatusCode(status);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_PROBLEM_JSON);
        DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(toJson(Map.of(
                "type", "about:blank",
                "title", message,
                "status", status.value(),
                "detail", message,
                "code", code,
                "message", message,
                "traceId", TraceIdWebFilter.resolveTraceId(exchange.getRequest()),
                "fieldErrors", List.of())));
        return exchange.getResponse().writeWith(Mono.just(buffer));
    }

    private byte[] toJson(Map<String, Object> body) {
        try {
            return objectMapper.writeValueAsBytes(body);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Cannot serialize security problem response", exception);
        }
    }
}
