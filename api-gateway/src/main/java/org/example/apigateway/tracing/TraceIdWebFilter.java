package org.example.apigateway.tracing;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.UUID;
import java.util.regex.Pattern;

@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class TraceIdWebFilter implements WebFilter {

    public static final String TRACE_ID_HEADER = "X-Trace-Id";
    private static final Pattern SAFE_TRACE_ID = Pattern.compile("^[A-Za-z0-9._-]{1,128}$");

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String traceId = normalize(exchange.getRequest().getHeaders().getFirst(TRACE_ID_HEADER));
        ServerHttpRequest request = exchange.getRequest().mutate()
                .headers(headers -> headers.set(TRACE_ID_HEADER, traceId))
                .build();
        ServerWebExchange tracedExchange = exchange.mutate().request(request).build();
        tracedExchange.getResponse().beforeCommit(() -> {
            tracedExchange.getResponse().getHeaders().set(TRACE_ID_HEADER, traceId);
            return Mono.empty();
        });

        long startedAt = System.nanoTime();
        return chain.filter(tracedExchange)
                .doFinally(signalType -> log.info(
                        "gateway_request method={} path={} status={} traceId={} durationMs={}",
                        request.getMethod(),
                        request.getPath().value(),
                        statusCode(tracedExchange),
                        traceId,
                        (System.nanoTime() - startedAt) / 1_000_000));
    }

    public static String resolveTraceId(ServerHttpRequest request) {
        return normalize(request.getHeaders().getFirst(TRACE_ID_HEADER));
    }

    private static String normalize(String traceId) {
        return traceId != null && SAFE_TRACE_ID.matcher(traceId).matches()
                ? traceId
                : UUID.randomUUID().toString();
    }

    private String statusCode(ServerWebExchange exchange) {
        HttpStatusCode statusCode = exchange.getResponse().getStatusCode();
        return statusCode == null ? "unknown" : Integer.toString(statusCode.value());
    }
}
