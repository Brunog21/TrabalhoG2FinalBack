package br.edu.atitus.gatewayservice.configs;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
public class CorsConfig {

    @Bean
    public CorsWebFilter corsWebFilter() {
        CorsConfiguration corsConfig = new CorsConfiguration();

        // Permite qualquer origem localhost (ex: porta 3000, 5173, 4200, etc.)
        corsConfig.setAllowedOriginPatterns(List.of("http://localhost:*"));

        // Métodos HTTP permitidos
        corsConfig.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));

        // Headers permitidos na requisição
        corsConfig.setAllowedHeaders(List.of("*"));

        // Permite envio de cookies/credenciais (necessário para JWT via cookie, se aplicável)
        corsConfig.setAllowCredentials(true);

        // Tempo que o browser pode cachear a resposta do preflight (em segundos)
        corsConfig.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // Aplica a configuração para todas as rotas do gateway
        source.registerCorsConfiguration("/**", corsConfig);

        return new CorsWebFilter(source);
    }
}
