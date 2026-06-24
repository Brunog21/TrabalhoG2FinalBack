package br.edu.atitus.productservice.controllers;

import br.edu.atitus.productservice.clients.CurrencyClient;
import br.edu.atitus.productservice.clients.CurrencyResponse;
import br.edu.atitus.productservice.dtos.ProductDTO;
import br.edu.atitus.productservice.entities.ProductEntity;
import br.edu.atitus.productservice.repositories.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProductController - Testes Unitários (Mockito)")
class ProductControllerTest {

    @Mock
    private ProductRepository repository;

    @Mock
    private CurrencyClient currencyClient;

    @Mock
    private CacheManager cacheManager;

    @InjectMocks
    private ProductController controller;

    private ProductEntity entity;

    @BeforeEach
    void setUp() {
        System.out.println("[DEBUG] ===== Setup do teste iniciado =====");
        // Injeta @Value("${server.port}") que não é resolvido pelo Mockito
        ReflectionTestUtils.setField(controller, "port", "8000");

        entity = new ProductEntity();
        entity.setId(1L);
        entity.setDescription("iPhone 15 128GB");
        entity.setBrand("Apple");
        entity.setModel("iPhone 15");
        entity.setCurrency("USD");
        entity.setPrice(799.00);
        entity.setStock(15);
        entity.setImageURL("https://example.com/iphone.jpg");

        System.out.println("[DEBUG] Entity configurada: " + entity.getDescription() + " | currency=USD | price=" + entity.getPrice());
    }

    @Test
    @DisplayName("getProduct com mesma moeda não deve converter preço")
    void getProduct_MesmaMoeda_DeveRetornarPrecoOriginal() throws Exception {
        System.out.println("[DEBUG] === Teste 1: targetCurrency == entity.currency (USD) ===");
        when(repository.findById(1L)).thenReturn(Optional.of(entity));

        ResponseEntity<ProductDTO> response = controller.getProduct(1L, "USD");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(799.00, response.getBody().convertedPrice(), 0.001);
        assertEquals("USD", response.getBody().requestCurrency());
        // Nunca consulta CurrencyClient nem Cache quando moedas são iguais
        verify(currencyClient, never()).getCurrency(any(), any());
        verify(cacheManager, never()).getCache(any());
        System.out.println("[DEBUG] ✓ Preço original retornado: " + response.getBody().convertedPrice());
    }

    @Test
    @DisplayName("getProduct com moeda diferente e cache miss deve chamar CurrencyClient")
    void getProduct_MoedaDiferente_CacheMiss_DeveConverterComClient() throws Exception {
        System.out.println("[DEBUG] === Teste 2: moeda diferente, cache miss, client retorna taxa ===");
        Cache mockCache = mock(Cache.class);
        when(cacheManager.getCache("ConvertedValue")).thenReturn(mockCache);
        when(mockCache.get("USD-BRL", Double.class)).thenReturn(null); // cache miss
        when(repository.findById(1L)).thenReturn(Optional.of(entity));
        when(currencyClient.getCurrency("USD", "BRL"))
                .thenReturn(new CurrencyResponse("USD", "BRL", 5.75, "port-8001"));

        ResponseEntity<ProductDTO> response = controller.getProduct(1L, "BRL");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        // 799.00 * 5.75 = 4594.25
        assertEquals(799.00 * 5.75, response.getBody().convertedPrice(), 0.01);
        assertTrue(response.getBody().environment().contains("port-8001"));
        verify(currencyClient, times(1)).getCurrency("USD", "BRL");
        verify(mockCache, times(1)).put("USD-BRL", 5.75);
        System.out.println("[DEBUG] ✓ Preço convertido: " + response.getBody().convertedPrice());
    }

    @Test
    @DisplayName("getProduct com cache miss e CurrencyClient retornando null deve retornar -1.0")
    void getProduct_CacheMiss_ClientNull_DeveRetornarMenos1() throws Exception {
        System.out.println("[DEBUG] === Teste 3: cache miss, client null → fallback -1.0 ===");
        Cache mockCache = mock(Cache.class);
        when(cacheManager.getCache("ConvertedValue")).thenReturn(mockCache);
        when(mockCache.get("USD-BRL", Double.class)).thenReturn(null);
        when(repository.findById(1L)).thenReturn(Optional.of(entity));
        when(currencyClient.getCurrency("USD", "BRL")).thenReturn(null); // fallback

        ResponseEntity<ProductDTO> response = controller.getProduct(1L, "BRL");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(-1.0, response.getBody().convertedPrice(), 0.001);
        assertTrue(response.getBody().environment().contains("Currency Fallback"));
        System.out.println("[DEBUG] ✓ Fallback ativo: convertedPrice=" + response.getBody().convertedPrice());
    }

    @Test
    @DisplayName("getProduct com cache hit deve usar taxa armazenada sem chamar CurrencyClient")
    void getProduct_CacheHit_DeveUsarTaxaDoCache() throws Exception {
        System.out.println("[DEBUG] === Teste 4: cache hit → usa taxa do cache ===");
        Cache mockCache = mock(Cache.class);
        when(cacheManager.getCache("ConvertedValue")).thenReturn(mockCache);
        when(mockCache.get("USD-BRL", Double.class)).thenReturn(5.50); // cache hit
        when(repository.findById(1L)).thenReturn(Optional.of(entity));

        ResponseEntity<ProductDTO> response = controller.getProduct(1L, "BRL");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        // 799.00 * 5.50 = 4394.50
        assertEquals(799.00 * 5.50, response.getBody().convertedPrice(), 0.01);
        assertTrue(response.getBody().environment().contains("Currency in cache"));
        // CurrencyClient NÃO deve ser chamado quando há cache hit
        verify(currencyClient, never()).getCurrency(any(), any());
        System.out.println("[DEBUG] ✓ Cache hit: convertedPrice=" + response.getBody().convertedPrice());
    }

    @Test
    @DisplayName("getProduct com ID inexistente deve lançar Exception")
    void getProduct_IdInexistente_DeveLancarException() {
        System.out.println("[DEBUG] === Teste 5: produto não encontrado ===");
        when(repository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(Exception.class, () -> controller.getProduct(999L, "BRL"));
        System.out.println("[DEBUG] ✓ Exception lançada para ID 999");
    }

    @Test
    @DisplayName("getProductNoConverter deve retornar produto com convertedPrice=-1.0 e sem conversão")
    void getProductNoConverter_ProdutoEncontrado_DeveRetornarSemConversao() throws Exception {
        System.out.println("[DEBUG] === Teste 6: getProductNoConverter produto encontrado ===");
        when(repository.findById(1L)).thenReturn(Optional.of(entity));

        ResponseEntity<ProductDTO> response = controller.getProductNoConverter(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(-1.0, response.getBody().convertedPrice(), 0.001);
        assertNull(response.getBody().requestCurrency()); // null pois não há conversão
        assertTrue(response.getBody().environment().contains("Product-service running on port: 8000"));
        verify(currencyClient, never()).getCurrency(any(), any());
        System.out.println("[DEBUG] ✓ convertedPrice=-1.0 | requestCurrency=null");
    }

    @Test
    @DisplayName("getProductNoConverter com ID inexistente deve lançar Exception")
    void getProductNoConverter_IdInexistente_DeveLancarException() {
        System.out.println("[DEBUG] === Teste 7: getProductNoConverter produto não encontrado ===");
        when(repository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(Exception.class, () -> controller.getProductNoConverter(999L));
        System.out.println("[DEBUG] ✓ Exception lançada para ID 999 em noconverter");
    }
}