package br.edu.atitus.currencyservice.controllers;

import br.edu.atitus.currencyservice.clients.BCBClient;
import br.edu.atitus.currencyservice.clients.BCBResponse;
import br.edu.atitus.currencyservice.entities.CurrencyEntity;
import br.edu.atitus.currencyservice.repositories.CurrencyRepository;
import br.edu.atitus.currencyservice.services.CurrencyConversionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.TestPropertySource;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CurrencyController.class)
@Import(CurrencyConversionService.class)
@TestPropertySource(properties = {
        "eureka.client.enabled=false",
        "eureka.client.register-with-eureka=false",
        "eureka.client.fetch-registry=false",
        "spring.cloud.openfeign.circuitbreaker.enabled=false",
        "management.health.circuitbreakers.enabled=false"
})
@DisplayName("CurrencyController - Testes REST com MockMvc (@WebMvcTest)")
class CurrencyControllerMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CurrencyRepository currencyRepository;

    @MockitoBean
    private BCBClient bcbClient;

    @MockitoBean
    private CacheManager cacheManager;

    private Cache cache;
    private CurrencyEntity currencyEntity;

    @BeforeEach
    void setUp() {
        System.out.println("[DEBUG] ===== Configurando mocks para MockMvc =====");

        currencyEntity = new CurrencyEntity();
        currencyEntity.setId(1L);
        currencyEntity.setSourceCurrency("USD");
        currencyEntity.setTargetCurrency("BRL");
        currencyEntity.setConversionRate(5.00);

        cache = mock(Cache.class);
        when(cacheManager.getCache("bcb-currency")).thenReturn(cache);
    }

    @Test
    @DisplayName("GET /currency/convert?source=USD&target=BRL → 200 OK com taxa do BCB")
    void getCurrency_ComBCBValido_DeveRetornar200ComTaxaBCB() throws Exception {
        System.out.println("[DEBUG] === Teste 1: GET com BCB ===");

        BCBResponse.BCBValue bcbValue = new BCBResponse.BCBValue();
        bcbValue.setCotacaoVenda(5.75);
        BCBResponse bcbResponse = new BCBResponse();
        bcbResponse.setValue(List.of(bcbValue));

        when(currencyRepository.findBySourceCurrencyAndTargetCurrency("USD", "BRL"))
                .thenReturn(Optional.of(currencyEntity));
        when(cache.get("USD")).thenReturn(null);
        when(bcbClient.getCotacaoBcb(anyString(), anyString())).thenReturn(bcbResponse);

        mockMvc.perform(get("/currency/convert")
                        .param("source", "USD")
                        .param("target", "BRL"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sourceCurrency").value("USD"))
                .andExpect(jsonPath("$.targetCurrency").value("BRL"))
                .andExpect(jsonPath("$.conversionRate").value(5.75));

        System.out.println("[DEBUG] ✓ 200 OK com taxa BCB: 5.75");
    }

    @Test
    @DisplayName("GET /currency/convert?source=XXX&target=BRL → RuntimeException 'Currency not found'")
    void getCurrency_MoedaInexistente_DeveGerarExcecaoCurrencyNotFound() throws Exception {
        System.out.println("[DEBUG] === Teste 2: GET com moeda inexistente ===");

        when(currencyRepository.findBySourceCurrencyAndTargetCurrency("XXX", "BRL"))
                .thenReturn(Optional.empty());

        MvcResult result = mockMvc.perform(get("/currency/convert")
                        .param("source", "XXX")
                        .param("target", "BRL"))
                .andDo(print())
                .andReturn();

        Exception resolvedException = result.getResolvedException();
        assertNotNull(resolvedException, "Deve ter gerado uma exceção");
        assertInstanceOf(RuntimeException.class, resolvedException,
                "A exceção deve ser RuntimeException");
        assertEquals("Currency not found", resolvedException.getMessage(),
                "Mensagem deve ser 'Currency not found'");

        System.out.println("[DEBUG] ✓ RuntimeException 'Currency not found' verificada via getResolvedException()");
    }

    @Test
    @DisplayName("GET /currency/convert → 200 OK com taxa do banco (fallback BCB)")
    void getCurrency_ComFallback_DeveRetornar200ComTaxaDoBanco() throws Exception {
        System.out.println("[DEBUG] === Teste 3: GET com fallback ===");

        when(currencyRepository.findBySourceCurrencyAndTargetCurrency("USD", "BRL"))
                .thenReturn(Optional.of(currencyEntity));
        when(cache.get("USD")).thenReturn(null);
        when(bcbClient.getCotacaoBcb(anyString(), anyString())).thenReturn(null);

        mockMvc.perform(get("/currency/convert")
                        .param("source", "USD")
                        .param("target", "BRL"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sourceCurrency").value("USD"))
                .andExpect(jsonPath("$.targetCurrency").value("BRL"))
                .andExpect(jsonPath("$.conversionRate").value(5.00));

        System.out.println("[DEBUG] ✓ 200 OK com taxa do banco: 5.00");
    }

    @Test
    @DisplayName("GET /currency/convert → 200 OK com taxa do cache")
    void getCurrency_ComCache_DeveRetornar200ComTaxaDoCache() throws Exception {
        System.out.println("[DEBUG] === Teste 4: GET com cache ===");

        Cache.ValueWrapper valueWrapper = mock(Cache.ValueWrapper.class);
        when(valueWrapper.get()).thenReturn(5.80);

        when(currencyRepository.findBySourceCurrencyAndTargetCurrency("USD", "BRL"))
                .thenReturn(Optional.of(currencyEntity));
        when(cache.get("USD")).thenReturn(valueWrapper);

        mockMvc.perform(get("/currency/convert")
                        .param("source", "USD")
                        .param("target", "BRL"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.conversionRate").value(5.80));

        verify(bcbClient, never()).getCotacaoBcb(anyString(), anyString());
        System.out.println("[DEBUG] ✓ 200 OK com taxa do cache: 5.80");
    }

    @Test
    @DisplayName("GET /currency/convert → JSON deve conter sourceCurrency, targetCurrency e conversionRate")
    void getCurrency_DeveRetornarCamposPrincipaisDoDTO() throws Exception {
        System.out.println("[DEBUG] === Teste 5: verificação campos do DTO ===");

        BCBResponse.BCBValue bcbValue = new BCBResponse.BCBValue();
        bcbValue.setCotacaoVenda(5.60);
        BCBResponse bcbResponse = new BCBResponse();
        bcbResponse.setValue(List.of(bcbValue));

        when(currencyRepository.findBySourceCurrencyAndTargetCurrency("USD", "BRL"))
                .thenReturn(Optional.of(currencyEntity));
        when(cache.get("USD")).thenReturn(null);
        when(bcbClient.getCotacaoBcb(anyString(), anyString())).thenReturn(bcbResponse);

        mockMvc.perform(get("/currency/convert")
                        .param("source", "USD")
                        .param("target", "BRL"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sourceCurrency").exists())
                .andExpect(jsonPath("$.targetCurrency").exists())
                .andExpect(jsonPath("$.conversionRate").exists())
                .andExpect(jsonPath("$.sourceCurrency").value("USD"))
                .andExpect(jsonPath("$.targetCurrency").value("BRL"))
                .andExpect(jsonPath("$.conversionRate").value(5.60));

        System.out.println("[DEBUG] ✓ Campos principais do DTO verificados");
    }

    @Test
    @DisplayName("GET /currency/convert sem parâmetros → 400 Bad Request")
    void getCurrency_SemParametros_DeveRetornar400() throws Exception {
        System.out.println("[DEBUG] === Teste 6: GET sem parâmetros ===");

        mockMvc.perform(get("/currency/convert"))
                .andDo(print())
                .andExpect(status().isBadRequest());

        System.out.println("[DEBUG] ✓ 400 para request sem parâmetros");
    }

    @Test
    @DisplayName("GET /currency/convert?target=BRL (sem source) → 400 Bad Request")
    void getCurrency_SomenteTarget_DeveRetornar400() throws Exception {
        System.out.println("[DEBUG] === Teste 7: GET sem source ===");

        mockMvc.perform(get("/currency/convert")
                        .param("target", "BRL"))
                .andDo(print())
                .andExpect(status().isBadRequest());

        System.out.println("[DEBUG] ✓ 400 para request sem source");
    }

    @Test
    @DisplayName("GET /currency/convert?source=BRL&target=USD → 200 OK com taxa inversa")
    void getCurrency_BrlParaUsd_DeveRetornarTaxaInversa() throws Exception {
        when(currencyRepository.findBySourceCurrencyAndTargetCurrency("BRL", "USD"))
                .thenReturn(Optional.empty());
        when(currencyRepository.findBySourceCurrencyAndTargetCurrency("USD", "BRL"))
                .thenReturn(Optional.of(currencyEntity));
        when(cache.get("USD")).thenReturn(null);

        BCBResponse.BCBValue bcbValue = new BCBResponse.BCBValue();
        bcbValue.setCotacaoVenda(5.00);
        BCBResponse bcbResponse = new BCBResponse();
        bcbResponse.setValue(List.of(bcbValue));
        when(bcbClient.getCotacaoBcb(anyString(), anyString())).thenReturn(bcbResponse);

        mockMvc.perform(get("/currency/convert")
                        .param("source", "BRL")
                        .param("target", "USD"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sourceCurrency").value("BRL"))
                .andExpect(jsonPath("$.targetCurrency").value("USD"))
                .andExpect(jsonPath("$.conversionRate").value(0.2));
    }
}