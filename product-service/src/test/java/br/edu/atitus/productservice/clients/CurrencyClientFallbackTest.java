package br.edu.atitus.productservice.clients;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("CurrencyClientFallback - Testes Unitários")
class CurrencyClientFallbackTest {

    private CurrencyClientFallback fallback;

    @BeforeEach
    void setUp() {
        System.out.println("[DEBUG] Criando instância de CurrencyClientFallback...");
        fallback = new CurrencyClientFallback();
    }

    @Test
    @DisplayName("Deve ser instanciável sem erros")
    void deveInstanciarSemErros() {
        System.out.println("[DEBUG] Verificando instanciação...");
        assertNotNull(fallback);
        System.out.println("[DEBUG] ✓ CurrencyClientFallback instanciado com sucesso");
    }

    @Test
    @DisplayName("getCurrency com parâmetros válidos deve retornar null (fallback)")
    void getCurrency_ComParametrosValidos_DeveRetornarNull() {
        System.out.println("[DEBUG] Testando fallback: source=USD, target=BRL...");

        CurrencyResponse result = fallback.getCurrency("USD", "BRL");

        assertNull(result);
        System.out.println("[DEBUG] ✓ Fallback retornou null para USD/BRL");
    }

    @Test
    @DisplayName("getCurrency com parâmetros nulos deve retornar null (fallback)")
    void getCurrency_ComParametrosNulos_DeveRetornarNull() {
        System.out.println("[DEBUG] Testando fallback com parâmetros nulos...");

        CurrencyResponse result = fallback.getCurrency(null, null);

        assertNull(result);
        System.out.println("[DEBUG] ✓ Fallback retornou null para parâmetros nulos");
    }
}