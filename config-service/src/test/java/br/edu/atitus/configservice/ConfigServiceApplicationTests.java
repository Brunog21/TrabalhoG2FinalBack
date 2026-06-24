package br.edu.atitus.configservice;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {
        "spring.profiles.active=native",
        "spring.cloud.config.server.native.search-locations=classpath:/config-test",
        "spring.cloud.config.server.git.uri=",
        "eureka.client.enabled=false",
        "eureka.client.register-with-eureka=false",
        "eureka.client.fetch-registry=false",
        "spring.cloud.discovery.enabled=false"
})
@DisplayName("ConfigService — Testes de contexto")
class ConfigServiceApplicationTests {

    @Test
    @DisplayName("Contexto completo sobe com @EnableConfigServer, fonte nativa e Eureka desabilitado")
    void contextLoads() {
        System.out.println("[DEBUG] Config service context loaded successfully");
    }
}