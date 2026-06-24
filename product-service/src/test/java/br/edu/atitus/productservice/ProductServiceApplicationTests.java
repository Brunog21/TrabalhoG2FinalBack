package br.edu.atitus.productservice;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("ProductServiceApplication - Contexto sobe corretamente")
class ProductServiceApplicationTests {

    @Test
    @DisplayName("Deve carregar o contexto da aplicação sem erros")
    void contextLoads() {
        System.out.println("[DEBUG] ✓ Contexto do product-service carregado com sucesso");
    }
}