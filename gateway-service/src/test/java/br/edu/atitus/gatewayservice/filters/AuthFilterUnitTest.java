package br.edu.atitus.gatewayservice.filters;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthFilter — Unitário Mockito (7 testes)")
class AuthFilterUnitTest {

    @Mock
    private GatewayFilterChain filterChain;

    private AuthFilter authFilter;

    private static final String SECRET = "chaveSuperSecretaParaJWTdeExemplo!@#123";

    @BeforeEach
    void setUp() {
        authFilter = new AuthFilter();
        System.out.println("[DEBUG] AuthFilter instanciado com construtor padrão");
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
    @DisplayName("Instância criável sem erro")
    void instance_createsWithoutError() {
        System.out.println("[DEBUG] Verificando instância de AuthFilter");
        assertThat(authFilter).isNotNull();
        System.out.println("[DEBUG] AuthFilter não é nulo — OK");
    }

    @Test
    @DisplayName("Rota pública /products/** — não começa com /ws/, chain chamado sem verificar token")
    void publicRoute_products_chainCalled_noTokenRequired() {
        System.out.println("[DEBUG] Testando rota pública: GET /products/1");

        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/products/1").build()
        );
        when(filterChain.filter(any(ServerWebExchange.class))).thenReturn(Mono.empty());

        authFilter.filter(exchange, filterChain).block();

        System.out.println("[DEBUG] Status (deve ser null — filter não bloqueou): "
                + exchange.getResponse().getStatusCode());

        verify(filterChain, times(1)).filter(any(ServerWebExchange.class));
        assertThat(exchange.getResponse().getStatusCode()).isNull();
        System.out.println("[DEBUG] /products/** não bloqueado pelo filter — OK");
    }

    @Test
    @DisplayName("Rota pública /auth/signup — não começa com /ws/, chain chamado sem verificar token")
    void publicRoute_auth_chainCalled_noTokenRequired() {
        System.out.println("[DEBUG] Testando rota pública: GET /auth/signup");

        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/auth/signup").build()
        );
        when(filterChain.filter(any(ServerWebExchange.class))).thenReturn(Mono.empty());

        authFilter.filter(exchange, filterChain).block();

        System.out.println("[DEBUG] Status (deve ser null — filter não bloqueou): "
                + exchange.getResponse().getStatusCode());

        verify(filterChain, times(1)).filter(any(ServerWebExchange.class));
        assertThat(exchange.getResponse().getStatusCode()).isNull();
        System.out.println("[DEBUG] /auth/signup não bloqueado pelo filter — OK");
    }

    @Test
    @DisplayName("Rota protegida /ws/orders sem header Authorization retorna 401, chain nunca chamado")
    void protectedRoute_noAuthHeader_returns401() {
        System.out.println("[DEBUG] Testando GET /ws/orders sem header Authorization");

        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/ws/orders").build()
        );

        authFilter.filter(exchange, filterChain).block();

        System.out.println("[DEBUG] Status: " + exchange.getResponse().getStatusCode());

        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        verify(filterChain, never()).filter(any());
        System.out.println("[DEBUG] 401 retornado para /ws/ sem Authorization — OK");
    }

    @Test
    @DisplayName("Rota protegida /ws/products com token inválido (garbage) retorna 401")
    void protectedRoute_invalidToken_returns401() {
        System.out.println("[DEBUG] Testando GET /ws/products/1 com token garbage");

        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/ws/products/1")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer token.invalido.garbage.aqui")
                        .build()
        );

        authFilter.filter(exchange, filterChain).block();

        System.out.println("[DEBUG] Status: " + exchange.getResponse().getStatusCode());

        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        verify(filterChain, never()).filter(any());
        System.out.println("[DEBUG] 401 para token inválido — OK");
    }

    @Test
    @DisplayName("Rota protegida /ws/orders com token expirado retorna 401")
    void protectedRoute_expiredToken_returns401() {
        String expired = tokenExpirado();
        System.out.println("[DEBUG] Testando GET /ws/orders com token expirado");

        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/ws/orders")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + expired)
                        .build()
        );

        authFilter.filter(exchange, filterChain).block();

        System.out.println("[DEBUG] Status: " + exchange.getResponse().getStatusCode());

        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        verify(filterChain, never()).filter(any());
        System.out.println("[DEBUG] 401 para token expirado — OK");
    }

    @Test
    @DisplayName("Token JWT válido — X-User-Id, X-User-Type e X-User-Email injetados, chain chamado")
    void validToken_addsAllThreeUserHeaders_chainCalled() {
        String jwt = tokenValido(42L, 1, "usuario@test.com");
        System.out.println("[DEBUG] Testando GET /ws/products/1 com token válido (id=42, type=1)");

        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/ws/products/1")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt)
                        .build()
        );
        when(filterChain.filter(any(ServerWebExchange.class))).thenReturn(Mono.empty());

        authFilter.filter(exchange, filterChain).block();

        verify(filterChain, times(1)).filter(argThat(ex -> {
            HttpHeaders headers = ex.getRequest().getHeaders();
            String userId    = headers.getFirst("X-User-Id");
            String userType  = headers.getFirst("X-User-Type");
            String userEmail = headers.getFirst("X-User-Email");
            System.out.println("[DEBUG] X-User-Id="    + userId);
            System.out.println("[DEBUG] X-User-Type="  + userType);
            System.out.println("[DEBUG] X-User-Email=" + userEmail);
            return "42".equals(userId)
                    && "1".equals(userType)
                    && "usuario@test.com".equals(userEmail);
        }));

        assertThat(exchange.getResponse().getStatusCode()).isNull();
        System.out.println("[DEBUG] Todos os headers injetados corretamente — OK");
    }
}