package com.example.api_gateway.config;


import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;

@Configuration
public class GatewayConfig {

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                // Route to User Service for Authentication (usually first or separate)
                .route("user-service-auth", r -> r.path("/api/auth/**")
                        .uri("lb://user-service"))

                // --- Payment Service Routes ---
                .route("strip-payment-gateway-service", r -> r.path("/api/payment/**")
                        .uri("lb://stripe-payment-gateway"))

                // --- Event Service Routes ---

                // **Specific User Actions first!**
                .route("event-service-user-actions", r -> r.path("/api/events/{eventId}/register", "/api/events/my-registrations")
                        // This route allows POST for register and GET for my-registrations
                        // No ADMIN check needed here, JWT filter already ensures authentication
                        .uri("lb://event-service"))

                // **Admin Write Operations next**
                .route("event-service-admin-write", r -> r.path("/api/events", "/api/events/{id}") // Be more specific if needed
                        .and()
                        .method(HttpMethod.POST, HttpMethod.PUT, HttpMethod.DELETE)
                        .filters(f -> f.filter((exchange, chain) -> {
                            // Check if the role from header is ADMIN
                            String role = exchange.getRequest().getHeaders().getFirst("X-User-Role");
                            if (!"ADMIN".equals(role)) {
                                exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN); // Use HttpStatus enum
                                return exchange.getResponse().setComplete();
                            }
                            // If ADMIN, continue the chain
                            return chain.filter(exchange);
                        }))
                        .uri("lb://event-service"))

                // **General Read Operations last for this service**
                .route("event-service-read", r -> r.path("/api/events/**") // Catches remaining GETs like /api/events and /api/events/{id}
                        .and()
                        .method(HttpMethod.GET)
                        // No specific role check needed here beyond authentication (done by JwtAuthenticationFilter)
                        .uri("lb://event-service"))


                // Add other service routes below if needed
                .build();
    }
}