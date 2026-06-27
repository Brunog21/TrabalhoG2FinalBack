package br.edu.atitus.gatewayservice.configs;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class GatewayConfig {

    @Bean
    RouteLocator getGatewayRoutes(RouteLocatorBuilder builder) {
        return builder.routes()
                .route(p -> p
                        .path("/get")
                        .filters(f -> f
                                .addRequestHeader("X-User-Name", "Pedro")
                                .dedupeResponseHeader("Access-Control-Allow-Origin Access-Control-Allow-Credentials", "RETAIN_UNIQUE"))
                        .uri("http://httpbin.org"))
                .route(p -> p
                        .path("/products/**")
                        .filters(f -> f.dedupeResponseHeader("Access-Control-Allow-Origin Access-Control-Allow-Credentials", "RETAIN_UNIQUE"))
                        .uri("lb://product-service"))
                .route(p -> p
                        .path("/ws/products/**")
                        .filters(f -> f.dedupeResponseHeader("Access-Control-Allow-Origin Access-Control-Allow-Credentials", "RETAIN_UNIQUE"))
                        .uri("lb://product-service"))
                .route(p -> p
                        .path("/currency/**")
                        .filters(f -> f.dedupeResponseHeader("Access-Control-Allow-Origin Access-Control-Allow-Credentials", "RETAIN_UNIQUE"))
                        .uri("lb://currency-service"))
                .route(p -> p
                        .path("/ws/currency/**")
                        .filters(f -> f.dedupeResponseHeader("Access-Control-Allow-Origin Access-Control-Allow-Credentials", "RETAIN_UNIQUE"))
                        .uri("lb://currency-service"))
                .route(p -> p
                        .path("/auth/**")
                        .filters(f -> f.dedupeResponseHeader("Access-Control-Allow-Origin Access-Control-Allow-Credentials", "RETAIN_UNIQUE"))
                        .uri("lb://auth-service"))
                .route(p -> p
                        .path("/ws/orders/**")
                        .filters(f -> f.dedupeResponseHeader("Access-Control-Allow-Origin Access-Control-Allow-Credentials", "RETAIN_UNIQUE"))
                        .uri("lb://order-service"))
                .build();
    }

}
