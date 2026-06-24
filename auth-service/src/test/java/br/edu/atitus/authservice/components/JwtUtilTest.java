package br.edu.atitus.authservice.components;

import br.edu.atitus.authservice.entities.UserType;

import io.jsonwebtoken.Claims;

import jakarta.servlet.http.HttpServletRequest;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("JwtUtil - Testes Unitários")
class JwtUtilTest {

    @Test
    @DisplayName("generateToken deve gerar token JWT não nulo e não vazio")
    void generateToken_DeveGerarTokenNaoVazio() {
        System.out.println("[DEBUG] Testando geração de token JWT...");

        String token = JwtUtil.generateToken("user@example.com", 1L, UserType.Common);

        assertNotNull(token, "Token não deve ser null");
        assertFalse(token.isEmpty(), "Token não deve ser vazio");
        // JWT tem formato: header.payload.signature (3 partes separadas por ponto)
        assertEquals(3, token.split("\\.").length, "Token deve ter 3 partes separadas por '.'");

        System.out.println("[DEBUG] ✓ Token gerado (primeiros 30 chars): " + token.substring(0, 30) + "...");
    }

    @Test
    @DisplayName("validateToken deve retornar Claims não nulas para token gerado")
    void validateToken_DeveRetornarClaimsParaTokenValido() {
        System.out.println("[DEBUG] Testando validação de token válido...");

        String token = JwtUtil.generateToken("user@example.com", 1L, UserType.Common);
        Claims claims = JwtUtil.validateToken(token);

        assertNotNull(claims, "Claims não devem ser null para token válido");

        System.out.println("[DEBUG] ✓ Claims retornadas com sucesso para token válido");
    }

    @Test
    @DisplayName("Claims do token devem conter email correto")
    void validateToken_ClaimsContemEmailCorreto() {
        System.out.println("[DEBUG] Testando campo 'email' nas Claims...");

        String token = JwtUtil.generateToken("test@empresa.com", 5L, UserType.Admin);
        Claims claims = JwtUtil.validateToken(token);

        assertNotNull(claims);
        assertEquals("test@empresa.com", claims.get("email", String.class));

        System.out.println("[DEBUG] ✓ Claims.email: " + claims.get("email", String.class));
    }

    @Test
    @DisplayName("Claims do token devem conter id e type (ordinal) corretos")
    void validateToken_ClaimsContemIdETipoCorretos() {
        System.out.println("[DEBUG] Testando campos 'id' e 'type' nas Claims...");

        String token = JwtUtil.generateToken("admin@empresa.com", 42L, UserType.Admin);
        Claims claims = JwtUtil.validateToken(token);

        assertNotNull(claims);

        long idValue = ((Number) claims.get("id")).longValue();
        assertEquals(42L, idValue, "id deve ser 42");

        int typeOrdinal = ((Number) claims.get("type")).intValue();
        assertEquals(0, typeOrdinal, "type deve ser 0 (Admin.ordinal())");

        System.out.println("[DEBUG] ✓ Claims.id=" + idValue + " | Claims.type=" + typeOrdinal
                + " (Admin.ordinal()=0)");
    }

    @Test
    @DisplayName("validateToken deve retornar null para token inválido ou corrompido")
    void validateToken_DeveRetornarNullParaTokenInvalido() {
        System.out.println("[DEBUG] Testando validação de token inválido...");

        Claims claims1 = JwtUtil.validateToken("token.invalido.assinatura");
        Claims claims2 = JwtUtil.validateToken("eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.corrompido.corrompido");
        Claims claims3 = JwtUtil.validateToken("");

        assertNull(claims1, "Token malformado deve retornar null");
        assertNull(claims2, "Token com assinatura corrompida deve retornar null");
        assertNull(claims3, "Token vazio deve retornar null");

        System.out.println("[DEBUG] ✓ Tokens inválidos retornam null (catch Exception interno)");
    }

    @Test
    @DisplayName("getJwtFromRequest deve extrair o token removendo o prefixo 'Bearer '")
    void getJwtFromRequest_DeveExtrairTokenDoHeader() {
        System.out.println("[DEBUG] Testando extração de JWT do header Authorization...");

        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("Authorization")).thenReturn("Bearer meu.token.jwt");

        String jwt = JwtUtil.getJwtFromRequest(request);

        assertEquals("meu.token.jwt", jwt);
        System.out.println("[DEBUG] ✓ JWT extraído: " + jwt);
    }

    @Test
    @DisplayName("getJwtFromRequest deve retornar null quando Authorization e authorization ausentes")
    void getJwtFromRequest_DeveRetornarNullQuandoHeaderAusente() {
        System.out.println("[DEBUG] Testando extração quando headers Authorization ausentes...");

        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("Authorization")).thenReturn(null);
        when(request.getHeader("authorization")).thenReturn(null);

        String jwt = JwtUtil.getJwtFromRequest(request);

        assertNull(jwt, "Deve retornar null quando ambos os headers estão ausentes");
        System.out.println("[DEBUG] ✓ null retornado para header ausente");
    }
}