package br.edu.atitus.productservice.controllers;

import br.edu.atitus.productservice.clients.CurrencyClient;
import br.edu.atitus.productservice.clients.CurrencyResponse;
import br.edu.atitus.productservice.entities.ProductEntity;
import br.edu.atitus.productservice.repositories.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductController.class)
@ActiveProfiles("test")
@DisplayName("ProductController - Testes REST com MockMvc (@WebMvcTest)")
class ProductControllerMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductRepository repository;

    @MockitoBean
    private CurrencyClient currencyClient;

    @MockitoBean
    private CacheManager cacheManager;

    private ProductEntity entity;
    private Cache mockCache;

    @BeforeEach
    void setUp() {
        System.out.println("[DEBUG] ===== Configurando mocks para MockMvc =====");

        entity = new ProductEntity();
        entity.setId(1L);
        entity.setDescription("iPhone 15 128GB");
        entity.setBrand("Apple");
        entity.setModel("iPhone 15");
        entity.setCurrency("USD");
        entity.setPrice(799.00);
        entity.setStock(15);
        entity.setImageURL("https://example.com/iphone.jpg");

        mockCache = mock(Cache.class);
        given(cacheManager.getCache("ConvertedValue")).willReturn(mockCache);
        given(mockCache.get(anyString(), any(Class.class))).willReturn(null); // cache miss padrão
    }

    @Test
    @DisplayName("GET /products/{id}?targetCurrency=USD com mesma moeda deve retornar 200 com preço original")
    void getProduct_MesmaMoeda_DeveRetornar200() throws Exception {
        System.out.println("[DEBUG] === Teste 1: GET /products/1?targetCurrency=USD (mesma moeda) ===");
        given(repository.findById(1L)).willReturn(Optional.of(entity));

        mockMvc.perform(get("/products/1").param("targetCurrency", "USD"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.description").value("iPhone 15 128GB"))
                .andExpect(jsonPath("$.convertedPrice").value(799.00))
                .andExpect(jsonPath("$.requestCurrency").value("USD"));

        System.out.println("[DEBUG] ✓ 200 OK com preço original USD");
    }

    @Test
    @DisplayName("GET /products/{id}?targetCurrency=BRL deve converter preço via CurrencyClient")
    void getProduct_MoedaDiferente_DeveConverter() throws Exception {
        System.out.println("[DEBUG] === Teste 2: GET /products/1?targetCurrency=BRL (conversão) ===");
        given(repository.findById(1L)).willReturn(Optional.of(entity));
        given(currencyClient.getCurrency("USD", "BRL"))
                .willReturn(new CurrencyResponse("USD", "BRL", 5.75, "port-8001"));

        mockMvc.perform(get("/products/1").param("targetCurrency", "BRL"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.convertedPrice").value(799.00 * 5.75))
                .andExpect(jsonPath("$.requestCurrency").value("BRL"))
                .andExpect(jsonPath("$.environment").value(org.hamcrest.Matchers.containsString("port-8001")));

        System.out.println("[DEBUG] ✓ 200 OK com preço convertido para BRL");
    }

    @Test
    @DisplayName("GET /products/{id} com ID inexistente deve retornar 400")
    void getProduct_IdInexistente_DeveRetornar400() throws Exception {
        System.out.println("[DEBUG] === Teste 3: GET /products/999?targetCurrency=BRL (não encontrado) ===");
        given(repository.findById(999L)).willReturn(Optional.empty());

        mockMvc.perform(get("/products/999").param("targetCurrency", "BRL"))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Product not found!"));

        System.out.println("[DEBUG] ✓ 400 para ID 999 com 'Product not found!'");
    }

    @Test
    @DisplayName("GET /products/noconverter/{id} deve retornar 200 com convertedPrice=-1.0")
    void getProductNoConverter_DeveRetornar200ComPrecoMenos1() throws Exception {
        System.out.println("[DEBUG] === Teste 4: GET /products/noconverter/1 ===");
        given(repository.findById(1L)).willReturn(Optional.of(entity));

        mockMvc.perform(get("/products/noconverter/1"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.convertedPrice").value(-1.0))
                .andExpect(jsonPath("$.requestCurrency").isEmpty());

        System.out.println("[DEBUG] ✓ 200 OK com convertedPrice=-1.0 e requestCurrency null");
    }

    @Test
    @DisplayName("GET /products/noconverter/{id} com ID inexistente deve retornar 400")
    void getProductNoConverter_IdInexistente_DeveRetornar400() throws Exception {
        System.out.println("[DEBUG] === Teste 5: GET /products/noconverter/999 (não encontrado) ===");
        given(repository.findById(999L)).willReturn(Optional.empty());

        mockMvc.perform(get("/products/noconverter/999"))
                .andDo(print())
                .andExpect(status().isBadRequest());

        System.out.println("[DEBUG] ✓ 400 para noconverter ID 999");
    }

    @Test
    @DisplayName("GET /products?targetCurrency=BRL deve retornar 200 com page de produtos")
    void getAllProducts_DeveRetornar200ComPaginacao() throws Exception {
        System.out.println("[DEBUG] === Teste 6: GET /products?targetCurrency=BRL (paginado) ===");
        given(repository.findAll(any(Pageable.class)))
                .willReturn(new PageImpl<>(List.of(entity)));
        given(currencyClient.getCurrency("USD", "BRL"))
                .willReturn(new CurrencyResponse("USD", "BRL", 5.75, "port-8001"));

        mockMvc.perform(get("/products").param("targetCurrency", "BRL"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].requestCurrency").value("BRL"))
                .andExpect(jsonPath("$.totalElements").value(1));

        System.out.println("[DEBUG] ✓ 200 OK com page de produtos convertidos");
    }
}