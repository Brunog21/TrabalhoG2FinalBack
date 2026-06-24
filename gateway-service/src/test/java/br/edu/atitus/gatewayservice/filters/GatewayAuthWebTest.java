package br.edu.atitus.gatewayservice.filters;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;

import javax.crypto.SecretKey;
import java.time.Duration;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {
        "eureka.client.enabled=false",
        "eureka.client.register-with-eureka=false",
        "eureka.client.fetch-registry=false",
        "spring.cloud.discovery.enabled=false"
})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("AuthFilter — REST WebTestClient (6 testes — integração web)")
class GatewayAuthWebTest {

    @Autowired
    private WebTestClient webTestClient;

    private static final String SECRET = "chaveSuperSecretaParaJWTdeExemplo!@#123";

    @BeforeEach
    void configureTimeout() {
        webTestClient = webTestClient.mutate()
                .responseTimeout(Duration.ofSeconds(5))
                .build();
        System.out.println("[DEBUG] WebTestClient configurado com timeout de 5s");
    }

    private String tokenValido(long id, int type, String email) {
        SecretKey key = Keys.hmacShaKeyFor(SECRET.getBytes());
        return Jwts.builder()
                .claim("id", id)
                .claim("type", type)
                .claim("email", email)
                .signWith(key)
                .compact();
    }

    private String tokenExpirado() {
        SecretKey key = Keys.hmacShaKeyFor(SECRET.getBytes());
        return Jwts.builder()
                .claim("id", 1L)
                .claim("type", 1)
                .claim("email", "expirado@test.com")
                .expiration(new Date(System.currentTimeMillis() - 10000))
                .signWith(key)
                .compact();
    }

    @Test
    @Order(1)
    @DisplayName("GET /ws/orders sem token retorna 401")
    void wsOrders_noToken_returns401() {
        System.out.println("[DEBUG] WebTest 1: GET /ws/orders sem token");

        webTestClient.get()
                .uri("/ws/orders")
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNAUTHORIZED);

        System.out.println("[DEBUG] 401 confirmado para /ws/orders sem token — OK");
    }

    @Test
    @Order(2)
    @DisplayName("GET /ws/products/{id} com token inválido (garbage) retorna 401")
    void wsProducts_invalidToken_returns401() {
        System.out.println("[DEBUG] WebTest 2: GET /ws/products/1 com token inválido");

        webTestClient.get()
                .uri("/ws/products/1")
                .header(HttpHeaders.AUTHORIZATION, "Bearer token.invalido.garbage")
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNAUTHORIZED);

        System.out.println("[DEBUG] 401 confirmado para token inválido — OK");
    }

    @Test
    @Order(3)
    @DisplayName("GET /ws/orders com Authorization sem prefixo 'Bearer ' retorna 401")
    void wsOrders_noBearerPrefix_returns401() {
        System.out.println("[DEBUG] WebTest 3: GET /ws/orders sem prefixo Bearer");

        webTestClient.get()
                .uri("/ws/orders")
                .header(HttpHeaders.AUTHORIZATION, "TokenSemPrefixoBearer")
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNAUTHORIZED);

        System.out.println("[DEBUG] 401 confirmado para Authorization sem 'Bearer ' — OK");
    }

    @Test
    @Order(4)
    @DisplayName("GET /ws/products/{id} com token expirado retorna 401")
    void wsProducts_expiredToken_returns401() {
        String expired = tokenExpirado();
        System.out.println("[DEBUG] WebTest 4: GET /ws/products/1 com token expirado");

        webTestClient.get()
                .uri("/ws/products/1")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + expired)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNAUTHORIZED);

        System.out.println("[DEBUG] 401 confirmado para token expirado — OK");
    }

    @Test
    @Order(5)
    @DisplayName("GET /products/{id} (rota pública) sem token — filter não bloqueia com 401")
    void publicRoute_products_noToken_filterDoesNotBlock() {
        System.out.println("[DEBUG] WebTest 5: GET /products/1 sem token (rota pública)");

        webTestClient.get()
                .uri("/products/1?targetCurrency=BRL")
                .exchange()
                .expectStatus().value(status -> {
                    System.out.println("[DEBUG] Status recebido: " + status
                            + " (esperado: qualquer coisa exceto 401)");
                    assertThat(status)
                            .as("Rota pública não deve ser bloqueada com 401 pelo AuthFilter")
                            .isNotEqualTo(HttpStatus.UNAUTHORIZED.value());
                });

        System.out.println("[DEBUG] /products/** não bloqueado pelo filter — OK");
    }

    @Test
    @Order(6)
    @DisplayName("GET /ws/orders com token válido — filter não bloqueia com 401")
    void wsOrders_validToken_filterDoesNotBlock() {
        String jwt = tokenValido(42L, 1, "usuario@test.com");
        System.out.println("[DEBUG] WebTest 6: GET /ws/orders com token válido (id=42, type=1)");

        webTestClient.get()
                .uri("/ws/orders?targetCurrency=BRL")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt)
                .exchange()
                .expectStatus().value(status -> {
                    System.out.println("[DEBUG] Status recebido: " + status
                            + " (esperado: qualquer coisa exceto 401)");
                    assertThat(status)
                            .as("Token válido não deve resultar em 401 — AuthFilter deve deixar passar")
                            .isNotEqualTo(HttpStatus.UNAUTHORIZED.value());
                });

        System.out.println("[DEBUG] Token válido passou pelo filter sem bloqueio — OK");
    }
}