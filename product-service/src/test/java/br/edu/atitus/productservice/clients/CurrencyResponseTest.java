package br.edu.atitus.productservice.clients;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("CurrencyResponse - Testes Unitários (record)")
class CurrencyResponseTest {

    @Test
    @DisplayName("Deve construir o record com todos os campos corretamente")
    void deveConstruirComTodosOsCampos() {
        System.out.println("[DEBUG] Testando construção de CurrencyResponse...");

        CurrencyResponse response = new CurrencyResponse("USD", "BRL", 5.75, "port-8001");

        assertNotNull(response);
        assertEquals("USD", response.sourceCurrency());
        assertEquals("BRL", response.targetCurrency());
        assertEquals(5.75, response.conversionRate());
        assertEquals("port-8001", response.environment());
        System.out.println("[DEBUG] ✓ CurrencyResponse: " + response);
    }

    @Test
    @DisplayName("sourceCurrency deve retornar o valor informado")
    void sourceCurrency_DeveRetornarValorInformado() {
        System.out.println("[DEBUG] Testando sourceCurrency...");
        CurrencyResponse response = new CurrencyResponse("EUR", "BRL", 6.0, null);

        assertEquals("EUR", response.sourceCurrency());
        System.out.println("[DEBUG] ✓ sourceCurrency: " + response.sourceCurrency());
    }

    @Test
    @DisplayName("targetCurrency deve retornar o valor informado")
    void targetCurrency_DeveRetornarValorInformado() {
        System.out.println("[DEBUG] Testando targetCurrency...");
        CurrencyResponse response = new CurrencyResponse("USD", "JPY", 150.0, null);

        assertEquals("JPY", response.targetCurrency());
        System.out.println("[DEBUG] ✓ targetCurrency: " + response.targetCurrency());
    }

    @Test
    @DisplayName("conversionRate deve retornar a taxa informada")
    void conversionRate_DeveRetornarTaxaInformada() {
        System.out.println("[DEBUG] Testando conversionRate...");
        CurrencyResponse response = new CurrencyResponse("USD", "BRL", 5.9875, "env");

        assertEquals(5.9875, response.conversionRate(), 0.0001);
        System.out.println("[DEBUG] ✓ conversionRate: " + response.conversionRate());
    }

    @Test
    @DisplayName("Deve aceitar environment nulo")
    void environment_AceitaNulo() {
        System.out.println("[DEBUG] Testando CurrencyResponse com environment null...");
        CurrencyResponse response = new CurrencyResponse("USD", "BRL", 5.75, null);

        assertNull(response.environment());
        System.out.println("[DEBUG] ✓ environment nulo aceito");
    }
}