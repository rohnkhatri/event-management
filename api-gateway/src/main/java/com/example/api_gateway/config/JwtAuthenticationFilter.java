package com.example.api_gateway.config;



import com.example.api_gateway.util.JwtUtil;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
@Slf4j
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

    @Autowired
    private JwtUtil jwtUtil;

    // List of endpoints that bypass JWT validation
    private static final List<String> publicApiEndpoints = List.of(
            "/api/auth/register",
            "/api/auth/login"
            // Add any other public paths, e.g., GET /api/events might be public
            // "/api/events" // Making GET public for example
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();
        log.debug("Processing request for path: {}", path);

        // Check if the path is public
        boolean isPublic = publicApiEndpoints.stream().anyMatch(path::startsWith);

        // Allow OPTIONS requests (pre-flight CORS)
        if (HttpMethod.OPTIONS.equals(request.getMethod())) {
            log.debug("Allowing OPTIONS request for path: {}", path);
            return chain.filter(exchange);
        }

        if (isPublic) {
            log.debug("Path {} is public, skipping JWT validation", path);
            return chain.filter(exchange);
        }

        // Check for Authorization header
        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.warn("Missing or invalid Authorization header for path: {}", path);
            return onError(exchange, "Missing or invalid Authorization header", HttpStatus.UNAUTHORIZED);
        }

        String token = authHeader.substring(7); // Remove "Bearer " prefix

        try {
            Claims claims = jwtUtil.validateToken(token);
            String username = jwtUtil.getUsernameFromToken(claims);
            String role = jwtUtil.getRoleFromToken(claims);
            Long userId = jwtUtil.getUserIdFromToken(claims); // Get userId

            log.debug("JWT validated successfully for user: {}, role: {}", username, role);

            // Add user info as headers for downstream services
            ServerHttpRequest mutatedRequest = request.mutate()
                    .header("X-Username", username)
                    .header("X-User-Role", role)
                    .header("X-User-Id", String.valueOf(userId)) // Pass userId
                    .build();

            ServerWebExchange mutatedExchange = exchange.mutate().request(mutatedRequest).build();
            return chain.filter(mutatedExchange);

        } catch (RuntimeException e) {
            log.error("JWT Validation Error for path {}: {}", path, e.getMessage());
            return onError(exchange, "Invalid or Expired Token: " + e.getMessage(), HttpStatus.UNAUTHORIZED);
        }
    }

    private Mono<Void> onError(ServerWebExchange exchange, String err, HttpStatus httpStatus) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(httpStatus);
        log.error("Auth Error: {}", err);
        return response.setComplete();
    }

    @Override
    public int getOrder() {
        // Run before other filters like routing (-1 is common for security filters)
        return -1;
    }
}