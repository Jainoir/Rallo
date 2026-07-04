package com.rallo.gateway.filter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Stamps every forwarded request with a shared secret so downstream services
 * can prove the request came through the gateway. Needed on platforms where
 * services get public URLs (e.g. Render free tier) and would otherwise trust
 * client-forgeable identity headers. Disabled when no secret is configured
 * (local development, private-network deployments).
 */
@Component
public class GatewaySecretRelayFilter implements GlobalFilter, Ordered {

    public static final String HEADER = "X-Gateway-Secret";

    @Value("${gateway.shared-secret:}")
    private String sharedSecret;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        if (sharedSecret == null || sharedSecret.isBlank()) {
            return chain.filter(exchange);
        }
        ServerWebExchange mutated = exchange.mutate()
                .request(r -> r.header(HEADER, sharedSecret))
                .build();
        return chain.filter(mutated);
    }

    @Override
    public int getOrder() {
        return -2;  // before JwtAuthFilter (-1) so all routed requests carry the secret
    }
}
