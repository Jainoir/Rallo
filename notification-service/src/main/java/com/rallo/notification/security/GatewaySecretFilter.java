package com.rallo.notification.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;

/**
 * Rejects requests that did not come through the API gateway. The gateway
 * stamps forwarded requests with a shared secret; without it, the
 * gateway-injected identity headers (X-User-Id) could be forged by anyone
 * who can reach this service's URL directly. Disabled when no secret is
 * configured (local development, private-network deployments).
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
public class GatewaySecretFilter extends OncePerRequestFilter {

    public static final String HEADER = "X-Gateway-Secret";

    /** Read-only endpoints that stay reachable without the gateway. */
    private static final List<String> EXEMPT_PREFIXES =
            List.of("/swagger-ui", "/api-docs", "/actuator");

    @Value("${gateway.shared-secret:}")
    private String sharedSecret;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        if (sharedSecret == null || sharedSecret.isBlank()) {
            return true;
        }
        String path = request.getRequestURI();
        return EXEMPT_PREFIXES.stream().anyMatch(path::startsWith);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String provided = request.getHeader(HEADER);
        if (provided != null && MessageDigest.isEqual(
                sharedSecret.getBytes(StandardCharsets.UTF_8),
                provided.getBytes(StandardCharsets.UTF_8))) {
            filterChain.doFilter(request, response);
        } else {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
        }
    }
}
