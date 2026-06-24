package br.edu.atitus.discoveryservice;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {
        "eureka.client.register-with-eureka=false",
        "eureka.client.fetch-registry=false",
        "eureka.server.enable-self-preservation=false",
        "management.endpoints.web.exposure.include=health,info"
})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("DiscoveryService — Actuator e Eureka endpoints (4 testes)")
class EurekaServerTest {

    @LocalServerPort
    private int port;

    private RestClient restClient;

    @BeforeEach
    void setUp() {
        restClient = RestClient.create("http://localhost:" + port);
        System.out.println("[DEBUG] RestClient configurado na porta: " + port);
    }

    @Test
    @Order(1)
    @DisplayName("GET /actuator/health retorna 200 com status UP")
    void healthEndpoint_returns200_statusUp() {
        System.out.println("[DEBUG] Testando GET /actuator/health");

        ResponseEntity<String> response = restClient.get()
                .uri("/actuator/health")
                .retrieve()
                .toEntity(String.class);

        System.out.println("[DEBUG] Status: " + response.getStatusCode());
        System.out.println("[DEBUG] Body: " + response.getBody());

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).contains("UP");
        System.out.println("[DEBUG] /actuator/health retornou UP — OK");
    }

    @Test
    @Order(2)
    @DisplayName("GET /actuator/info retorna 200 com nome da aplicação")
    void infoEndpoint_returns200_withAppName() {
        System.out.println("[DEBUG] Testando GET /actuator/info");

        ResponseEntity<String> response = restClient.get()
                .uri("/actuator/info")
                .retrieve()
                .toEntity(String.class);

        System.out.println("[DEBUG] Status: " + response.getStatusCode());
        System.out.println("[DEBUG] Body: " + response.getBody());

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).contains("discovery-service");
        System.out.println("[DEBUG] /actuator/info retornou nome da aplicação — OK");
    }

    @Test
    @Order(3)
    @DisplayName("GET /eureka/apps retorna 200 — registry de aplicações acessível")
    void eurekaAppsEndpoint_returns200() {
        System.out.println("[DEBUG] Testando GET /eureka/apps");

        ResponseEntity<String> response = restClient.get()
                .uri("/eureka/apps")
                .retrieve()
                .toEntity(String.class);

        System.out.println("[DEBUG] Status: " + response.getStatusCode());
        System.out.println("[DEBUG] Body: " + response.getBody());

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        System.out.println("[DEBUG] /eureka/apps respondeu 200 — OK");
    }

    @Test
    @Order(4)
    @DisplayName("GET /eureka/apps retorna resposta com campo applications")
    void eurekaAppsEndpoint_returnsApplicationsField() {
        System.out.println("[DEBUG] Verificando estrutura da resposta de /eureka/apps");

        ResponseEntity<String> response = restClient.get()
                .uri("/eureka/apps")
                .retrieve()
                .toEntity(String.class);

        System.out.println("[DEBUG] Status: " + response.getStatusCode());
        System.out.println("[DEBUG] Body: " + response.getBody());

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).contains("applications");
        System.out.println("[DEBUG] Campo 'applications' presente na resposta — OK");
    }
}