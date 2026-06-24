package br.edu.atitus.productservice.controllers;

import br.edu.atitus.productservice.entities.ProductEntity;
import br.edu.atitus.productservice.repositories.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.cache.CacheManager;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(WsProductController.class)
@ActiveProfiles("test")
@DisplayName("WsProductController - Testes REST com MockMvc (@WebMvcTest)")
class WsProductControllerMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductRepository repository;

    /**
     * WsProductController não usa CacheManager diretamente,
     * mas @EnableCaching na ProductServiceApplication exige um CacheManager no contexto.
     * @MockitoBean fornece o mock necessário para o contexto @WebMvcTest subir.
     */
    @MockitoBean
    private CacheManager cacheManager;

    private ProductEntity savedEntity;

    private static final String PRODUCT_JSON = """
            {
                "description": "iPhone 15 128GB",
                "brand": "Apple",
                "model": "iPhone 15",
                "currency": "USD",
                "price": 799.00,
                "imageURL": "https://example.com/iphone.jpg"
            }
            """;

    @BeforeEach
    void setUp() {
        System.out.println("[DEBUG] ===== Configurando mocks para WsProductController MockMvc =====");

        savedEntity = new ProductEntity();
        savedEntity.setId(1L);
        savedEntity.setDescription("iPhone 15 128GB");
        savedEntity.setBrand("Apple");
        savedEntity.setModel("iPhone 15");
        savedEntity.setCurrency("USD");
        savedEntity.setPrice(799.00);
        savedEntity.setStock(10);
        savedEntity.setImageURL("https://example.com/iphone.jpg");
    }

    @Test
    @DisplayName("POST /ws/products com X-User-Type=0 deve retornar 201 e salvar produto")
    void postProduct_TypeAdmin_DeveRetornar201() throws Exception {
        System.out.println("[DEBUG] === Teste 1: POST com X-User-Type=0 (admin) ===");
        given(repository.save(any(ProductEntity.class))).willReturn(savedEntity);

        mockMvc.perform(post("/ws/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(PRODUCT_JSON)
                        .header("X-User-Id", 1L)
                        .header("X-User-Email", "admin@test.com")
                        .header("X-User-Type", 0))
                .andDo(print())
                .andExpect(status().isCreated())
                // id é null com mock pois o controller não captura o retorno de repository.save()
                // Em JPA real, save() preenche o id no objeto por referência; com mock, não
                .andExpect(jsonPath("$.description").value("iPhone 15 128GB"))
                .andExpect(jsonPath("$.brand").value("Apple"))
                .andExpect(jsonPath("$.stock").value(10));

        System.out.println("[DEBUG] ✓ 201 Created para admin");
    }

    @Test
    @DisplayName("POST /ws/products com X-User-Type != 0 deve retornar 400")
    void postProduct_TypeNaoAdmin_DeveRetornar400() throws Exception {
        System.out.println("[DEBUG] === Teste 2: POST com X-User-Type=1 (não admin) ===");

        mockMvc.perform(post("/ws/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(PRODUCT_JSON)
                        .header("X-User-Id", 2L)
                        .header("X-User-Email", "user@test.com")
                        .header("X-User-Type", 1))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Usuário sem Permissão!"));

        System.out.println("[DEBUG] ✓ 400 com 'Usuário sem Permissão!' para type=1");
    }

    @Test
    @DisplayName("PUT /ws/products/{id} com X-User-Type=0 deve retornar 200")
    void putProduct_TypeAdmin_DeveRetornar200() throws Exception {
        System.out.println("[DEBUG] === Teste 3: PUT com X-User-Type=0 (admin) ===");
        given(repository.save(any(ProductEntity.class))).willReturn(savedEntity);

        mockMvc.perform(put("/ws/products/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(PRODUCT_JSON)
                        .header("X-User-Id", 1L)
                        .header("X-User-Email", "admin@test.com")
                        .header("X-User-Type", 0))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.description").value("iPhone 15 128GB"));

        System.out.println("[DEBUG] ✓ 200 OK para PUT admin");
    }

    @Test
    @DisplayName("PUT /ws/products/{id} com X-User-Type != 0 deve retornar 400")
    void putProduct_TypeNaoAdmin_DeveRetornar400() throws Exception {
        System.out.println("[DEBUG] === Teste 4: PUT com X-User-Type=2 (não admin) ===");

        mockMvc.perform(put("/ws/products/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(PRODUCT_JSON)
                        .header("X-User-Id", 2L)
                        .header("X-User-Email", "user@test.com")
                        .header("X-User-Type", 2))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Usuário sem Permissão!"));

        System.out.println("[DEBUG] ✓ 400 para PUT type=2");
    }

    @Test
    @DisplayName("DELETE /ws/products/{id} com X-User-Type=0 deve retornar 200 'Excluído'")
    void deleteProduct_TypeAdmin_DeveRetornar200() throws Exception {
        System.out.println("[DEBUG] === Teste 5: DELETE com X-User-Type=0 (admin) ===");
        doNothing().when(repository).deleteById(1L);

        mockMvc.perform(delete("/ws/products/1")
                        .header("X-User-Id", 1L)
                        .header("X-User-Email", "admin@test.com")
                        .header("X-User-Type", 0))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string("Excluído"));

        System.out.println("[DEBUG] ✓ 200 OK 'Excluído' para DELETE admin");
    }

    @Test
    @DisplayName("DELETE /ws/products/{id} com X-User-Type != 0 deve retornar 400")
    void deleteProduct_TypeNaoAdmin_DeveRetornar400() throws Exception {
        System.out.println("[DEBUG] === Teste 6: DELETE com X-User-Type=5 (não admin) ===");

        mockMvc.perform(delete("/ws/products/1")
                        .header("X-User-Id", 2L)
                        .header("X-User-Email", "user@test.com")
                        .header("X-User-Type", 5))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Usuário sem Permissão!"));

        System.out.println("[DEBUG] ✓ 400 para DELETE type=5");
    }
}