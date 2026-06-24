package br.edu.atitus.gatewayservice.components;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("JwtUtil — Unitário (7 testes)")
class JwtUtilTest {

    private static final String SECRET = "chaveSuperSecretaParaJWTdeExemplo!@#123";

    private String gerarToken(long id, int type, String email) {
        SecretKey key = Keys.hmacShaKeyFor(SECRET.getBytes());
        return Jwts.builder()
                .claim("id", id)
                .claim("type", type)
                .claim("email", email)
                .signWith(key)
                .compact();
    }

    private String gerarTokenExpirado() {
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
    @DisplayName("validateToken com token válido retorna Claims não nulas")
    void validateToken_validToken_returnsNonNullClaims() {
        String token = gerarToken(1L, 0, "admin@test.com");
        System.out.println("[DEBUG] Token gerado: " + token.substring(0, 20) + "...");

        Claims claims = JwtUtil.validateToken(token);

        System.out.println("[DEBUG] Claims retornadas: " + claims);
        assertThat(claims).isNotNull();
        System.out.println("[DEBUG] Claims não nulas — OK");
    }

    @Test
    @DisplayName("validateToken com token válido retorna claim 'id' correta")
    void validateToken_validToken_returnsCorrectId() {
        String token = gerarToken(42L, 1, "user@test.com");
        System.out.println("[DEBUG] Testando claim 'id' com valor esperado 42");

        Claims claims = JwtUtil.validateToken(token);

        assertThat(claims).isNotNull();
        Long id = claims.get("id", Long.class);
        System.out.println("[DEBUG] id retornado: " + id);
        assertThat(id).isEqualTo(42L);
        System.out.println("[DEBUG] Claim 'id' correta — OK");
    }

    @Test
    @DisplayName("validateToken com token válido retorna claim 'type' correta")
    void validateToken_validToken_returnsCorrectType() {
        String token = gerarToken(10L, 1, "user@test.com");
        System.out.println("[DEBUG] Testando claim 'type' com valor esperado 1");

        Claims claims = JwtUtil.validateToken(token);

        assertThat(claims).isNotNull();
        Integer type = claims.get("type", Integer.class);
        System.out.println("[DEBUG] type retornado: " + type);
        assertThat(type).isEqualTo(1);
        System.out.println("[DEBUG] Claim 'type' correta — OK");
    }

    @Test
    @DisplayName("validateToken com token válido retorna claim 'email' correta")
    void validateToken_validToken_returnsCorrectEmail() {
        String token = gerarToken(5L, 0, "admin@atitus.com");
        System.out.println("[DEBUG] Testando claim 'email' com valor esperado admin@atitus.com");

        Claims claims = JwtUtil.validateToken(token);

        assertThat(claims).isNotNull();
        String email = claims.get("email", String.class);
        System.out.println("[DEBUG] email retornado: " + email);
        assertThat(email).isEqualTo("admin@atitus.com");
        System.out.println("[DEBUG] Claim 'email' correta — OK");
    }

    @Test
    @DisplayName("validateToken com token inválido (garbage) retorna null")
    void validateToken_garbageToken_returnsNull() {
        System.out.println("[DEBUG] Testando token garbage: 'isto.nao.e.um.jwt'");

        Claims claims = JwtUtil.validateToken("isto.nao.e.um.jwt");

        System.out.println("[DEBUG] Claims retornadas: " + claims);
        assertThat(claims).isNull();
        System.out.println("[DEBUG] Null retornado para token garbage — OK");
    }

    @Test
    @DisplayName("validateToken com token expirado retorna null")
    void validateToken_expiredToken_returnsNull() {
        String expired = gerarTokenExpirado();
        System.out.println("[DEBUG] Testando token expirado: " + expired.substring(0, 20) + "...");

        Claims claims = JwtUtil.validateToken(expired);

        System.out.println("[DEBUG] Claims retornadas: " + claims);
        assertThat(claims).isNull();
        System.out.println("[DEBUG] Null retornado para token expirado — OK");
    }

    @Test
    @DisplayName("validateToken com string vazia retorna null")
    void validateToken_emptyString_returnsNull() {
        System.out.println("[DEBUG] Testando token vazio: ''");

        Claims claims = JwtUtil.validateToken("");

        System.out.println("[DEBUG] Claims retornadas: " + claims);
        assertThat(claims).isNull();
        System.out.println("[DEBUG] Null retornado para string vazia — OK");
    }
}