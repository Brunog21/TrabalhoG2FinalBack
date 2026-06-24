package br.edu.atitus.order_service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("OrderServiceApplication - Contexto sobe corretamente")
class OrderServiceApplicationTests {

    @Test
    @DisplayName("Contexto do order-service carrega sem erros com H2 e Eureka desabilitado")
    void contextLoads() {
        System.out.println("[DEBUG] ✓ Contexto do order-service carregado com sucesso");
    }
}