package br.edu.atitus.order_service.clients;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("CurrencyResponse - Testes Unitários (POJO)")
class CurrencyResponseTest {

    @Test
    @DisplayName("Instância criável e campos com valores padrão")
    void deveInstanciarComValoresPadrao() {
        System.out.println("[DEBUG] Testando instanciação de CurrencyResponse...");

        CurrencyResponse response = new CurrencyResponse();

        assertNotNull(response);
        assertNull(response.getSourceCurrency(),  "sourceCurrency deve ser null");
        assertNull(response.getTargetCurrency(),  "targetCurrency deve ser null");
        assertEquals(0.0, response.getConversionRate(), 0.0001,
                "conversionRate deve ser 0.0 por padrão");
        assertNull(response.getEnviroment(), "enviroment deve ser null");

        System.out.println("[DEBUG] ✓ CurrencyResponse instanciado com valores padrão");
    }

    @Test
    @DisplayName("setSourceCurrency e getSourceCurrency funcionam")
    void deveSetarEObterSourceCurrency() {
        System.out.println("[DEBUG] Testando sourceCurrency...");

        CurrencyResponse response = new CurrencyResponse();
        response.setSourceCurrency("USD");

        assertEquals("USD", response.getSourceCurrency());
        System.out.println("[DEBUG] ✓ sourceCurrency: " + response.getSourceCurrency());
    }

    @Test
    @DisplayName("setTargetCurrency e getTargetCurrency funcionam")
    void deveSetarEObterTargetCurrency() {
        System.out.println("[DEBUG] Testando targetCurrency...");

        CurrencyResponse response = new CurrencyResponse();
        response.setTargetCurrency("BRL");

        assertEquals("BRL", response.getTargetCurrency());
        System.out.println("[DEBUG] ✓ targetCurrency: " + response.getTargetCurrency());
    }

    @Test
    @DisplayName("setConversionRate e getConversionRate preservam precisão decimal")
    void deveSetarEObterConversionRate() {
        System.out.println("[DEBUG] Testando conversionRate...");

        CurrencyResponse response = new CurrencyResponse();
        response.setConversionRate(5.7543);

        assertEquals(5.7543, response.getConversionRate(), 0.0001);
        System.out.println("[DEBUG] ✓ conversionRate: " + response.getConversionRate());
    }

    @Test
    @DisplayName("setEnviroment e getEnviroment funcionam (typo preservado no código de produção)")
    void deveSetarEObterEnviroment() {
        System.out.println("[DEBUG] Testando campo 'enviroment' (typo do código de produção)...");

        CurrencyResponse response = new CurrencyResponse();
        response.setEnviroment("currency-port-8001");

        assertEquals("currency-port-8001", response.getEnviroment());
        System.out.println("[DEBUG] ✓ enviroment: " + response.getEnviroment());
    }

    @Test
    @DisplayName("Campos de instâncias distintas são independentes")
    void todosCamposSaoIndependentes() {
        System.out.println("[DEBUG] Testando independência entre instâncias...");

        CurrencyResponse r1 = new CurrencyResponse();
        r1.setSourceCurrency("USD");
        r1.setTargetCurrency("BRL");
        r1.setConversionRate(5.75);
        r1.setEnviroment("env-1");

        CurrencyResponse r2 = new CurrencyResponse();
        r2.setSourceCurrency("EUR");
        r2.setTargetCurrency("JPY");
        r2.setConversionRate(160.0);
        r2.setEnviroment("env-2");

        assertEquals("USD", r1.getSourceCurrency());
        assertEquals("EUR", r2.getSourceCurrency());
        assertEquals("BRL", r1.getTargetCurrency());
        assertEquals("JPY", r2.getTargetCurrency());
        assertEquals(5.75,  r1.getConversionRate(), 0.001);
        assertEquals(160.0, r2.getConversionRate(), 0.001);
        assertEquals("env-1", r1.getEnviroment());
        assertEquals("env-2", r2.getEnviroment());

        System.out.println("[DEBUG] ✓ r1=USD/BRL/5.75 | r2=EUR/JPY/160.0 — independentes");
    }
}