package br.edu.atitus.currencyservice.clients;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DisplayName("BCBClientFallback - Testes Unitários")
class BCBClientFallbackTest {

    private BCBClientFallback fallback;

    @BeforeEach
    void setUp() {
        System.out.println("[DEBUG] Criando instância de BCBClientFallback...");
        fallback = new BCBClientFallback();
    }

    @Test
    @DisplayName("Deve retornar null para moeda USD com data válida")
    void getCotacaoBcb_USD_DeveSempreRetornarNull() {
        System.out.println("[DEBUG] Testando fallback: moeda=USD, data=05-14-2026");

        BCBResponse response = fallback.getCotacaoBcb("USD", "05-14-2026");

        assertNull(response,
                "Fallback deve retornar null para sinalizar falha ao CurrencyController");

        System.out.println("[DEBUG] ✓ Null retornado para USD — fallback funcionando");
    }

    @Test
    @DisplayName("Deve retornar null para moeda EUR")
    void getCotacaoBcb_EUR_DeveSempreRetornarNull() {
        System.out.println("[DEBUG] Testando fallback: moeda=EUR");

        BCBResponse response = fallback.getCotacaoBcb("EUR", "05-14-2026");

        assertNull(response, "Fallback deve retornar null independente da moeda");

        System.out.println("[DEBUG] ✓ Null retornado para EUR");
    }

    @Test
    @DisplayName("Deve retornar null mesmo com parâmetros vazios")
    void getCotacaoBcb_ParametrosVazios_DeveSempreRetornarNull() {
        System.out.println("[DEBUG] Testando fallback com parâmetros vazios...");

        BCBResponse response = fallback.getCotacaoBcb("", "");

        assertNull(response, "Fallback deve retornar null mesmo com strings vazias");

        System.out.println("[DEBUG] ✓ Null retornado para parâmetros vazios");
    }

    @Test
    @DisplayName("Deve retornar null mesmo com parâmetros nulos")
    void getCotacaoBcb_ParametrosNulos_DeveSempreRetornarNull() {
        System.out.println("[DEBUG] Testando fallback com parâmetros nulos...");

        BCBResponse response = fallback.getCotacaoBcb(null, null);

        assertNull(response, "Fallback deve retornar null mesmo com null como argumentos");

        System.out.println("[DEBUG] ✓ Null retornado para parâmetros nulos");
    }

    @Test
    @DisplayName("Instância de BCBClientFallback deve ser criada com sucesso")
    void bcbClientFallback_DeveSerInstanciavel() {
        System.out.println("[DEBUG] Verificando se BCBClientFallback é instanciável...");

        BCBClientFallback novaInstancia = new BCBClientFallback();

        assertNotNull(novaInstancia, "Instância não deve ser nula");
        System.out.println("[DEBUG] ✓ BCBClientFallback instanciado com sucesso");
    }
}