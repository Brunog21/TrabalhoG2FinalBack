package br.edu.atitus.currencyservice.controllers;

import br.edu.atitus.currencyservice.clients.BCBClient;
import br.edu.atitus.currencyservice.clients.BCBResponse;
import br.edu.atitus.currencyservice.dtos.CurrencyDTO;
import br.edu.atitus.currencyservice.entities.CurrencyEntity;
import br.edu.atitus.currencyservice.repositories.CurrencyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CurrencyController - Testes Unitários (Mockito)")
class CurrencyControllerTest {

    @Mock
    private CurrencyRepository currencyRepository;

    @Mock
    private BCBClient bcbClient;

    @Mock
    private CacheManager cacheManager;

    @Mock
    private Environment environment;

    @Mock
    private Cache cache;

    @InjectMocks
    private CurrencyController currencyController;

    private CurrencyEntity currencyEntity;

    @BeforeEach
    void setUp() {
        System.out.println("[DEBUG] ===== Setup do teste iniciado =====");

        currencyEntity = new CurrencyEntity();
        currencyEntity.setId(1L);
        currencyEntity.setSourceCurrency("USD");
        currencyEntity.setTargetCurrency("BRL");
        currencyEntity.setConversionRate(5.00);

        System.out.println("[DEBUG] Entity configurada: "
                + currencyEntity.getSourceCurrency() + "/"
                + currencyEntity.getTargetCurrency()
                + " → rate=" + currencyEntity.getConversionRate());
    }

    @Test
    @DisplayName("Deve retornar taxa do BCB quando cache está vazio")
    void getCurrency_ComRespostaBCB_SemCache_DeveRetornarTaxaBCB() {
        System.out.println("[DEBUG] === Teste 1: cotação BCB sem cache ===");

        BCBResponse.BCBValue bcbValue = new BCBResponse.BCBValue();
        bcbValue.setCotacaoVenda(5.75);
        BCBResponse bcbResponse = new BCBResponse();
        bcbResponse.setValue(List.of(bcbValue));

        when(currencyRepository.findBySourceCurrencyAndTargetCurrency("USD", "BRL"))
                .thenReturn(Optional.of(currencyEntity));
        when(environment.getProperty("local.server.port")).thenReturn("8001");
        when(cacheManager.getCache("bcb-currency")).thenReturn(cache);
        when(cache.get("USD")).thenReturn(null);
        when(bcbClient.getCotacaoBcb(anyString(), anyString())).thenReturn(bcbResponse);

        ResponseEntity<CurrencyDTO> response = currencyController.getCurrency("USD", "BRL");

        System.out.println("[DEBUG] Status: " + response.getStatusCode());
        System.out.println("[DEBUG] Body: " + response.getBody());

        assertNotNull(response, "Response não deve ser nula");
        assertEquals(HttpStatus.OK, response.getStatusCode(), "Status deve ser 200 OK");
        assertNotNull(response.getBody(), "Body não deve ser nulo");
        assertEquals("USD", response.getBody().sourceCurrency());
        assertEquals("BRL", response.getBody().targetCurrency());
        assertEquals(5.75, response.getBody().conversionRate(), 0.001,
                "Taxa deve vir do BCB (5.75), não do banco (5.00)");

        verify(cache, times(1)).put("USD", 5.75);
        System.out.println("[DEBUG] ✓ Taxa BCB utilizada e persistida no cache");
    }

    @Test
    @DisplayName("Deve retornar taxa do cache e não chamar o BCB")
    void getCurrency_ComCacheHit_NaoDeveChamarBCB() {
        System.out.println("[DEBUG] === Teste 2: cache hit ===");

        Cache.ValueWrapper valueWrapper = mock(Cache.ValueWrapper.class);
        when(valueWrapper.get()).thenReturn(5.80);

        when(currencyRepository.findBySourceCurrencyAndTargetCurrency("USD", "BRL"))
                .thenReturn(Optional.of(currencyEntity));
        when(environment.getProperty("local.server.port")).thenReturn("8001");
        when(cacheManager.getCache("bcb-currency")).thenReturn(cache);
        when(cache.get("USD")).thenReturn(valueWrapper);

        ResponseEntity<CurrencyDTO> response = currencyController.getCurrency("USD", "BRL");

        System.out.println("[DEBUG] Body: " + response.getBody());

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(5.80, response.getBody().conversionRate(), 0.001,
                "Taxa deve vir do cache (5.80)");
        assertTrue(response.getBody().environment().contains(" - BCB in cache"),
                "Environment deve indicar que veio do cache");

        verify(bcbClient, never()).getCotacaoBcb(anyString(), anyString());
        System.out.println("[DEBUG] ✓ BCB não foi chamado — cache utilizado corretamente");
    }

    @Test
    @DisplayName("Deve usar taxa do banco quando BCB retorna null (fallback)")
    void getCurrency_ComFallbackBCBNull_DeveUsarTaxaDoBanco() {
        System.out.println("[DEBUG] === Teste 3: fallback BCB null ===");

        when(currencyRepository.findBySourceCurrencyAndTargetCurrency("USD", "BRL"))
                .thenReturn(Optional.of(currencyEntity));
        when(environment.getProperty("local.server.port")).thenReturn("8001");
        when(cacheManager.getCache("bcb-currency")).thenReturn(cache);
        when(cache.get("USD")).thenReturn(null);
        when(bcbClient.getCotacaoBcb(anyString(), anyString())).thenReturn(null);

        ResponseEntity<CurrencyDTO> response = currencyController.getCurrency("USD", "BRL");

        System.out.println("[DEBUG] Body: " + response.getBody());

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(5.00, response.getBody().conversionRate(), 0.001,
                "Taxa deve vir do banco (5.00) quando BCB está em fallback");
        assertTrue(response.getBody().environment().contains(" - BCB Fallback"),
                "Environment deve indicar fallback ativo");

        verify(cache, never()).put(anyString(), any());
        System.out.println("[DEBUG] ✓ Taxa do banco usada — fallback ativo corretamente");
    }

    @Test
    @DisplayName("Deve usar taxa do banco quando BCB retorna lista vazia")
    void getCurrency_ComListaBCBVazia_DeveUsarTaxaDoBanco() {
        System.out.println("[DEBUG] === Teste 4: BCB lista vazia ===");

        BCBResponse emptyResponse = new BCBResponse();
        emptyResponse.setValue(List.of());

        when(currencyRepository.findBySourceCurrencyAndTargetCurrency("USD", "BRL"))
                .thenReturn(Optional.of(currencyEntity));
        when(environment.getProperty("local.server.port")).thenReturn("8001");
        when(cacheManager.getCache("bcb-currency")).thenReturn(cache);
        when(cache.get("USD")).thenReturn(null);
        when(bcbClient.getCotacaoBcb(anyString(), anyString())).thenReturn(emptyResponse);

        ResponseEntity<CurrencyDTO> response = currencyController.getCurrency("USD", "BRL");

        System.out.println("[DEBUG] Body: " + response.getBody());
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(5.00, response.getBody().conversionRate(), 0.001,
                "Taxa deve vir do banco quando lista BCB é vazia");
        System.out.println("[DEBUG] ✓ Taxa do banco usada para lista BCB vazia");
    }

    @Test
    @DisplayName("Deve usar taxa do banco quando BCB retorna value nulo")
    void getCurrency_ComValueBCBNulo_DeveUsarTaxaDoBanco() {
        System.out.println("[DEBUG] === Teste 5: BCB value nulo ===");

        BCBResponse responseWithNullValue = new BCBResponse();
        responseWithNullValue.setValue(null);

        when(currencyRepository.findBySourceCurrencyAndTargetCurrency("USD", "BRL"))
                .thenReturn(Optional.of(currencyEntity));
        when(environment.getProperty("local.server.port")).thenReturn("8001");
        when(cacheManager.getCache("bcb-currency")).thenReturn(cache);
        when(cache.get("USD")).thenReturn(null);
        when(bcbClient.getCotacaoBcb(anyString(), anyString())).thenReturn(responseWithNullValue);

        ResponseEntity<CurrencyDTO> response = currencyController.getCurrency("USD", "BRL");

        System.out.println("[DEBUG] Body: " + response.getBody());
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(5.00, response.getBody().conversionRate(), 0.001,
                "Taxa deve vir do banco quando value do BCB é nulo");
        System.out.println("[DEBUG] ✓ Taxa do banco usada para value BCB nulo");
    }

    @Test
    @DisplayName("Deve lançar RuntimeException quando par de moedas não existe no banco")
    void getCurrency_MoedaNaoEncontrada_DeveLancarRuntimeException() {
        System.out.println("[DEBUG] === Teste 6: par de moedas inexistente ===");

        when(currencyRepository.findBySourceCurrencyAndTargetCurrency("XXX", "BRL"))
                .thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> currencyController.getCurrency("XXX", "BRL"),
                "Deve lançar RuntimeException para par de moedas inexistente"
        );

        assertEquals("Currency not found", exception.getMessage(),
                "Mensagem da exceção deve ser 'Currency not found'");

        System.out.println("[DEBUG] ✓ RuntimeException lançada: " + exception.getMessage());

        verifyNoInteractions(bcbClient);
        verifyNoInteractions(cacheManager);
        System.out.println("[DEBUG] ✓ BCBClient e CacheManager não foram chamados");
    }

    @Test
    @DisplayName("Deve funcionar corretamente com par de moedas EUR/USD")
    void getCurrency_ParEURUSD_DeveRetornarCorretamente() {
        System.out.println("[DEBUG] === Teste 7: par EUR/USD ===");

        CurrencyEntity eurEntity = new CurrencyEntity();
        eurEntity.setId(2L);
        eurEntity.setSourceCurrency("EUR");
        eurEntity.setTargetCurrency("USD");
        eurEntity.setConversionRate(1.08);

        BCBResponse.BCBValue bcbValue = new BCBResponse.BCBValue();
        bcbValue.setCotacaoVenda(6.20);
        BCBResponse bcbResponse = new BCBResponse();
        bcbResponse.setValue(List.of(bcbValue));

        when(currencyRepository.findBySourceCurrencyAndTargetCurrency("EUR", "USD"))
                .thenReturn(Optional.of(eurEntity));
        when(environment.getProperty("local.server.port")).thenReturn("8001");
        when(cacheManager.getCache("bcb-currency")).thenReturn(cache);
        when(cache.get("EUR")).thenReturn(null);
        when(bcbClient.getCotacaoBcb(eq("EUR"), anyString())).thenReturn(bcbResponse);

        ResponseEntity<CurrencyDTO> response = currencyController.getCurrency("EUR", "USD");

        System.out.println("[DEBUG] Body: " + response.getBody());
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("EUR", response.getBody().sourceCurrency());
        assertEquals("USD", response.getBody().targetCurrency());
        assertEquals(6.20, response.getBody().conversionRate(), 0.001,
                "Taxa deve ser 6.20 (BCB), não 1.08 (banco)");

        verify(cache, times(1)).put("EUR", 6.20);
        System.out.println("[DEBUG] ✓ Par EUR/USD funcionou corretamente");
    }
}