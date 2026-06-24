package br.edu.atitus.authservice;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("AuthServiceApplication - Contexto sobe corretamente")
class AuthServiceApplicationTests {

    @Test
    @DisplayName("Contexto do auth-service carrega sem erros (Security + JPA + H2 + Eureka desabilitado)")
    void contextLoads() {
        System.out.println("[DEBUG] ✓ Contexto do auth-service carregado com sucesso");
    }
}