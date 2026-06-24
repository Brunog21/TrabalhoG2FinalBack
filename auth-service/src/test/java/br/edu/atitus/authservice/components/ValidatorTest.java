package br.edu.atitus.authservice.components;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

// Validator é uma classe utilitária com método estático — sem Spring context necessário

@DisplayName("Validator - Testes Unitários")
class ValidatorTest {

    // -----------------------------------------------------------------------
    // Teste 1 — email válido retorna true
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("Email válido deve retornar true")
    void emailValido_DeveRetornarTrue() {
        System.out.println("[DEBUG] Testando email válido...");

        assertTrue(Validator.validateEmail("user@example.com"));
        assertTrue(Validator.validateEmail("admin@empresa.com.br"));
        assertTrue(Validator.validateEmail("first.last+tag@sub.domain.io"));

        System.out.println("[DEBUG] ✓ Emails válidos retornam true");
    }

    // -----------------------------------------------------------------------
    // Teste 2 — email nulo retorna false
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("Email nulo deve retornar false")
    void emailNulo_DeveRetornarFalse() {
        System.out.println("[DEBUG] Testando email nulo...");

        assertFalse(Validator.validateEmail(null));

        System.out.println("[DEBUG] ✓ null retorna false");
    }

    // -----------------------------------------------------------------------
    // Teste 3 — email vazio retorna false
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("Email vazio deve retornar false")
    void emailVazio_DeveRetornarFalse() {
        System.out.println("[DEBUG] Testando email vazio...");

        assertFalse(Validator.validateEmail(""));
        assertFalse(Validator.validateEmail("   ".trim())); // só espaços

        System.out.println("[DEBUG] ✓ String vazia retorna false");
    }

    // -----------------------------------------------------------------------
    // Teste 4 — email sem @ retorna false
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("Email sem @ deve retornar false")
    void emailSemArroba_DeveRetornarFalse() {
        System.out.println("[DEBUG] Testando email sem '@'...");

        assertFalse(Validator.validateEmail("userexample.com"));
        assertFalse(Validator.validateEmail("plaintext"));

        System.out.println("[DEBUG] ✓ Email sem '@' retorna false");
    }

    // -----------------------------------------------------------------------
    // Teste 5 — email sem extensão de domínio retorna false
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("Email sem extensão de domínio deve retornar false")
    void emailSemExtensaoDeDominio_DeveRetornarFalse() {
        System.out.println("[DEBUG] Testando email sem extensão de domínio...");

        // Regex exige: (?:[a-zA-Z0-9-]+\.)+[a-zA-Z]{2,7}
        // 'user@example' não tem ponto após o @
        assertFalse(Validator.validateEmail("user@example"));
        // 'user@' sem domínio algum
        assertFalse(Validator.validateEmail("user@"));

        System.out.println("[DEBUG] ✓ Emails sem extensão de domínio retornam false");
    }
}