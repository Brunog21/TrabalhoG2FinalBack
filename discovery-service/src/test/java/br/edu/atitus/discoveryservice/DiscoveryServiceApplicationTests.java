package br.edu.atitus.discoveryservice;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {
        "eureka.client.register-with-eureka=false",
        "eureka.client.fetch-registry=false",
        "eureka.server.enable-self-preservation=false"
})
@DisplayName("DiscoveryService — Testes de contexto")
class DiscoveryServiceApplicationTests {

    @Test
    @DisplayName("Contexto completo sobe com @EnableEurekaServer standalone e Eureka client desabilitado")
    void contextLoads() {
        System.out.println("[DEBUG] Discovery service context loaded successfully");
    }
}