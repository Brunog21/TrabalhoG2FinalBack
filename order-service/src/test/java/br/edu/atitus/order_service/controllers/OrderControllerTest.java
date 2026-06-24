package br.edu.atitus.order_service.controllers;

import br.edu.atitus.order_service.clients.ProductClient;
import br.edu.atitus.order_service.clients.ProductResponse;
import br.edu.atitus.order_service.dtos.OrderDTO;
import br.edu.atitus.order_service.dtos.OrderItemDTO;
import br.edu.atitus.order_service.entities.OrderEntity;
import br.edu.atitus.order_service.entities.OrderItemEntity;
import br.edu.atitus.order_service.services.OrderService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrderController - Testes Unitários (Mockito)")
class OrderControllerTest {

    @Mock
    private OrderService orderService;

    @Mock
    private ProductClient productClient;

    @InjectMocks
    private OrderController controller;

    private ProductResponse productResponse;
    private Pageable pageable;

    @BeforeEach
    void setUp() {
        System.out.println("[DEBUG] ===== Setup do teste iniciado =====");

        productResponse = new ProductResponse(
                1L, "iPhone 15 128GB", "Apple", "iPhone 15",
                799.0, "USD", 10,
                "https://example.com/iphone.jpg", "product-port-8001", 0.0
        );

        pageable = PageRequest.of(0, 5, Sort.by("orderDate").ascending());

        System.out.println("[DEBUG] ProductResponse e Pageable configurados");
    }

    @Test
    @DisplayName("createOrder com DTO válido deve retornar 201 com a order")
    void createOrder_DeveRetornar201() {
        System.out.println("[DEBUG] === Teste 1: createOrder retorna 201 ===");

        when(productClient.getProductById(1L)).thenReturn(productResponse);

        OrderDTO dto = new OrderDTO(List.of(new OrderItemDTO(1L, 2)));

        ResponseEntity<OrderEntity> response =
                controller.createOrder(dto, 1L, "user@test.com", 0);

        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());

        OrderEntity body = response.getBody();
        assertEquals(1L, body.getCustomerId());
        assertNotNull(body.getOrderDate());

        System.out.println("[DEBUG] ✓ 201 Created | customerId=" + body.getCustomerId());
    }

    @Test
    @DisplayName("createOrder chama ProductClient para cada item do DTO")
    void createOrder_DeveChamarProductClientPorItem() {
        System.out.println("[DEBUG] === Teste 2: createOrder chama productClient por item ===");

        when(productClient.getProductById(1L)).thenReturn(productResponse);
        when(productClient.getProductById(2L)).thenReturn(productResponse);

        OrderDTO dto = new OrderDTO(List.of(
                new OrderItemDTO(1L, 1),
                new OrderItemDTO(2L, 3)
        ));

        controller.createOrder(dto, 1L, "user@test.com", 0);

        verify(productClient, times(1)).getProductById(1L);
        verify(productClient, times(1)).getProductById(2L);
        // orderService.createOrder chamado 1x
        verify(orderService, times(1)).createOrder(any(OrderEntity.class), eq(1L));

        System.out.println("[DEBUG] ✓ productClient chamado 2x (1 por item) | orderService.createOrder chamado 1x");
    }

    @Test
    @DisplayName("listOrdersByUser retorna 200 com página de orders do cliente")
    void listOrdersByUser_DeveRetornar200() {
        System.out.println("[DEBUG] === Teste 3: listOrdersByUser retorna 200 ===");

        OrderEntity order = new OrderEntity();
        order.setId(1L);
        order.setCustomerId(1L);
        order.setOrderDate(LocalDateTime.now());

        Page<OrderEntity> page = new PageImpl<>(List.of(order));
        when(orderService.findOrdersByCustomerId(eq(1L), eq("BRL"), any(Pageable.class)))
                .thenReturn(page);

        ResponseEntity<Page<OrderEntity>> response =
                controller.listOrdersByUser("BRL", pageable, 1L, "user@test.com", 0);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().getTotalElements());

        System.out.println("[DEBUG] ✓ 200 OK | totalElements=" + response.getBody().getTotalElements());
    }

    @Test
    @DisplayName("listOrdersByUser converte targetCurrency para maiúsculo antes de chamar o serviço")
    void listOrdersByUser_DeveConverterTargetCurrencyParaMaiusculo() {
        System.out.println("[DEBUG] === Teste 4: targetCurrency convertido para maiúsculo ===");

        Page<OrderEntity> paginaVazia = new PageImpl<>(List.of());
        when(orderService.findOrdersByCustomerId(eq(1L), eq("BRL"), any(Pageable.class)))
                .thenReturn(paginaVazia);

        ResponseEntity<Page<OrderEntity>> response =
                controller.listOrdersByUser("brl", pageable, 1L, "user@test.com", 0);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(orderService).findOrdersByCustomerId(1L, "BRL", pageable);

        System.out.println("[DEBUG] ✓ 'brl' convertido para 'BRL' antes de chamar orderService");
    }
}