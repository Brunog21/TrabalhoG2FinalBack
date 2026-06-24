package br.edu.atitus.gatewayservice;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {
		"eureka.client.enabled=false",
		"eureka.client.register-with-eureka=false",
		"eureka.client.fetch-registry=false",
		"spring.cloud.discovery.enabled=false"
})
@DisplayName("GatewayService — Testes de contexto")
class GatewayServiceApplicationTests {

	@Test
	@DisplayName("Contexto completo sobe com WebFlux, AuthFilter, GatewayConfig e Eureka desabilitado")
	void contextLoads() {
		System.out.println("[DEBUG] Gateway context loaded successfully");
	}
}