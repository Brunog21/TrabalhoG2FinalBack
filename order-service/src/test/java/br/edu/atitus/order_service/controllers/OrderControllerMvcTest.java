package br.edu.atitus.order_service.controllers;

import br.edu.atitus.order_service.clients.ProductClient;
import br.edu.atitus.order_service.clients.ProductResponse;
import br.edu.atitus.order_service.entities.OrderEntity;
import br.edu.atitus.order_service.services.OrderService;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OrderController.class)
@ActiveProfiles("test")
@DisplayName("OrderController - Testes REST com MockMvc (@WebMvcTest)")
class OrderControllerMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private OrderService orderService;

    @MockitoBean
    private ProductClient productClient;

    private static final String ORDER_JSON = """
            {
                "items": [
                    {
                        "productId": 1,
                        "quantity": 2
                    }
                ]
            }
            """;

    private ProductResponse novoProduct() {
        return new ProductResponse(
                1L, "iPhone 15 128GB", "Apple", "iPhone 15",
                799.0, "USD", 10,
                "https://example.com/iphone.jpg", "product-port-8001", 0.0
        );
    }

    @Test
    @DisplayName("POST /ws/orders com body e headers válidos deve retornar 201 com itens")
    void postOrder_ComHeadersValidos_DeveRetornar201() throws Exception {
        System.out.println("[DEBUG] === Teste 1: POST /ws/orders com headers válidos ===");

        given(productClient.getProductById(1L)).willReturn(novoProduct());
        given(orderService.createOrder(any(OrderEntity.class), eq(1L))).willAnswer(i -> i.getArgument(0));

        mockMvc.perform(post("/ws/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(ORDER_JSON)
                        .header("X-User-Id",    1L)
                        .header("X-User-Email", "user@test.com")
                        .header("X-User-Type",  0))
                .andDo(print())
                .andExpect(status().isCreated())
                // controller retorna o objeto 'order' local (id=null pois save() retorno é descartado)
                .andExpect(jsonPath("$.items").isArray())
                .andExpect(jsonPath("$.items[0].productId").value(1))
                .andExpect(jsonPath("$.items[0].quantity").value(2))
                .andExpect(jsonPath("$.items[0].priceAtPurchase").value(799.0))
                .andExpect(jsonPath("$.items[0].currencyAtPurchase").value("USD"));

        System.out.println("[DEBUG] ✓ 201 Created com item correto");
    }

    @Test
    @DisplayName("POST /ws/orders sem header X-User-Id deve retornar 400")
    void postOrder_SemHeaderUserId_DeveRetornar400() throws Exception {
        System.out.println("[DEBUG] === Teste 2: POST /ws/orders sem X-User-Id ===");

        mockMvc.perform(post("/ws/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(ORDER_JSON)
                        .header("X-User-Email", "user@test.com")
                        .header("X-User-Type",  0))
                .andDo(print())
                .andExpect(status().isBadRequest());

        System.out.println("[DEBUG] ✓ 400 para POST sem X-User-Id");
    }

    @Test
    @DisplayName("GET /ws/orders?targetCurrency=BRL com headers válidos deve retornar 200 com página")
    void getOrders_ComTargetCurrency_DeveRetornar200() throws Exception {
        System.out.println("[DEBUG] === Teste 3: GET /ws/orders?targetCurrency=BRL ===");

        OrderEntity order = new OrderEntity();
        order.setId(1L);
        order.setOrderDate(LocalDateTime.of(2026, 6, 22, 10, 0, 0));
        order.setItems(new ArrayList<>());
        order.setTotalPrice(799.0);
        order.setTotalConvertedPrice(4594.25);

        given(orderService.findOrdersByCustomerId(eq(1L), eq("BRL"), any()))
                .willReturn(new PageImpl<>(List.of(order), PageRequest.of(0, 5), 1));

        mockMvc.perform(get("/ws/orders")
                        .param("targetCurrency", "BRL")
                        .header("X-User-Id",    1L)
                        .header("X-User-Email", "user@test.com")
                        .header("X-User-Type",  0))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].totalPrice").value(799.0))
                .andExpect(jsonPath("$.content[0].totalConvertedPrice").value(4594.25))
                .andExpect(jsonPath("$.totalElements").value(1));

        System.out.println("[DEBUG] ✓ 200 OK com página de orders");
    }

    @Test
    @DisplayName("GET /ws/orders sem parâmetro targetCurrency deve retornar 400")
    void getOrders_SemTargetCurrency_DeveRetornar400() throws Exception {
        System.out.println("[DEBUG] === Teste 4: GET /ws/orders sem targetCurrency ===");

        mockMvc.perform(get("/ws/orders")
                        .header("X-User-Id",    1L)
                        .header("X-User-Email", "user@test.com")
                        .header("X-User-Type",  0))
                .andDo(print())
                .andExpect(status().isBadRequest());

        System.out.println("[DEBUG] ✓ 400 para GET sem targetCurrency");
    }

    @Test
    @DisplayName("GET /ws/orders sem header X-User-Id deve retornar 400")
    void getOrders_SemHeaderUserId_DeveRetornar400() throws Exception {
        System.out.println("[DEBUG] === Teste 5: GET /ws/orders sem X-User-Id ===");

        mockMvc.perform(get("/ws/orders")
                        .param("targetCurrency", "BRL")
                        .header("X-User-Email", "user@test.com")
                        .header("X-User-Type",  0))
                .andDo(print())
                .andExpect(status().isBadRequest());

        System.out.println("[DEBUG] ✓ 400 para GET sem X-User-Id");
    }
}