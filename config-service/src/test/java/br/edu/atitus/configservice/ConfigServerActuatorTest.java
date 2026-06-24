package br.edu.atitus.configservice;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {
        "spring.profiles.active=native",
        "spring.cloud.config.server.native.search-locations=classpath:/config-test",
        "spring.cloud.config.server.git.uri=",
        "eureka.client.enabled=false",
        "eureka.client.register-with-eureka=false",
        "eureka.client.fetch-registry=false",
        "spring.cloud.discovery.enabled=false",
        "management.endpoints.web.exposure.include=health,info,env"
})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("ConfigService — Actuator e Config Server (4 testes)")
class ConfigServerActuatorTest {

    @LocalServerPort
    private int port;

    private final RestTemplate restTemplate = new RestTemplate();

    @Test
    @Order(1)
    @DisplayName("GET /actuator/health retorna 200 com status UP")
    void healthEndpoint_returns200_statusUp() {
        System.out.println("[DEBUG] Testando GET /actuator/health na porta " + port);

        ResponseEntity<String> response = restTemplate.getForEntity(
                "http://localhost:" + port + "/actuator/health", String.class);

        System.out.println("[DEBUG] Status: " + response.getStatusCode());
        System.out.println("[DEBUG] Body: " + response.getBody());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("UP");
        System.out.println("[DEBUG] /actuator/health retornou UP — OK");
    }

    @Test
    @Order(2)
    @DisplayName("GET /actuator/info retorna 200 com nome da aplicação")
    void infoEndpoint_returns200_withAppName() {
        System.out.println("[DEBUG] Testando GET /actuator/info na porta " + port);

        ResponseEntity<String> response = restTemplate.getForEntity(
                "http://localhost:" + port + "/actuator/info", String.class);

        System.out.println("[DEBUG] Status: " + response.getStatusCode());
        System.out.println("[DEBUG] Body: " + response.getBody());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("config-service");
        System.out.println("[DEBUG] /actuator/info retornou nome da aplicação — OK");
    }

    @Test
    @Order(3)
    @DisplayName("GET /application/default retorna 200 com estrutura válida do config server")
    void configEndpoint_applicationDefault_returns200() {
        System.out.println("[DEBUG] Testando GET /application/default na porta " + port);

        ResponseEntity<String> response = restTemplate.getForEntity(
                "http://localhost:" + port + "/application/default", String.class);

        System.out.println("[DEBUG] Status: " + response.getStatusCode());
        System.out.println("[DEBUG] Body: " + response.getBody());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("application");
        assertThat(response.getBody()).contains("propertySources");
        System.out.println("[DEBUG] Config server retornou estrutura esperada — OK");
    }

    @Test
    @Order(4)
    @DisplayName("GET /application/default retorna propriedades do arquivo de teste")
    void configEndpoint_applicationDefault_containsTestProperties() {
        System.out.println("[DEBUG] Verificando se config-test/application.properties está sendo lido");

        ResponseEntity<String> response = restTemplate.getForEntity(
                "http://localhost:" + port + "/application/default", String.class);

        System.out.println("[DEBUG] Status: " + response.getStatusCode());
        System.out.println("[DEBUG] Body: " + response.getBody());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("configuracao-de-teste");
        System.out.println("[DEBUG] Propriedade app.test.mensagem encontrada — OK");
    }
}