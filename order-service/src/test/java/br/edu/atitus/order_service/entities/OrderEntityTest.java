package br.edu.atitus.order_service.entities;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("OrderEntity - Testes Unitários")
class OrderEntityTest {

    @Test
    @DisplayName("Instância com todos os campos nulos/zero por padrão")
    void deveInstanciarComValoresPadrao() {
        System.out.println("[DEBUG] Criando instância de OrderEntity...");

        OrderEntity order = new OrderEntity();

        assertNotNull(order);
        assertNull(order.getId(),         "id deve ser null");
        assertNull(order.getOrderDate(),  "orderDate deve ser null");
        assertNull(order.getCustomerId(), "customerId deve ser null (@JsonIgnore)");
        assertNull(order.getItems(),      "items deve ser null");
        assertEquals(0.0, order.getTotalPrice(),          0.001, "totalPrice default 0.0 (@Transient)");
        assertEquals(0.0, order.getTotalConvertedPrice(), 0.001, "totalConvertedPrice default 0.0 (@Transient)");

        System.out.println("[DEBUG] ✓ Todos os campos nulos/zero por padrão");
    }

    @Test
    @DisplayName("setId e getId funcionam")
    void deveSetarEObterI() {
        System.out.println("[DEBUG] Testando setId/getId...");

        OrderEntity order = new OrderEntity();
        order.setId(42L);

        assertEquals(42L, order.getId());
        System.out.println("[DEBUG] ✓ id: " + order.getId());
    }

    @Test
    @DisplayName("setOrderDate e getOrderDate funcionam")
    void deveSetarEObterOrderDate() {
        System.out.println("[DEBUG] Testando setOrderDate/getOrderDate...");

        OrderEntity order = new OrderEntity();
        LocalDateTime agora = LocalDateTime.of(2026, 6, 22, 14, 30, 0);
        order.setOrderDate(agora);

        assertEquals(agora, order.getOrderDate());
        System.out.println("[DEBUG] ✓ orderDate: " + order.getOrderDate());
    }

    @Test
    @DisplayName("setCustomerId e getCustomerId funcionam (@JsonIgnore não impede acesso Java)")
    void deveSetarEObterCustomerId() {
        System.out.println("[DEBUG] Testando setCustomerId/getCustomerId...");

        OrderEntity order = new OrderEntity();
        order.setCustomerId(10L);

        assertEquals(10L, order.getCustomerId());
        System.out.println("[DEBUG] ✓ customerId: " + order.getCustomerId() + " (@JsonIgnore — não serializado)");
    }

    @Test
    @DisplayName("setItems e getItems funcionam")
    void deveSetarEObterItems() {
        System.out.println("[DEBUG] Testando setItems/getItems...");

        OrderEntity order = new OrderEntity();
        OrderItemEntity item = new OrderItemEntity();
        item.setProductId(1L);
        item.setQuantity(3);
        List<OrderItemEntity> items = List.of(item);

        order.setItems(items);

        assertNotNull(order.getItems());
        assertEquals(1, order.getItems().size());
        assertEquals(1L, order.getItems().get(0).getProductId());
        assertEquals(3,  order.getItems().get(0).getQuantity());

        System.out.println("[DEBUG] ✓ items com " + order.getItems().size() + " item(ns)");
    }

    @Test
    @DisplayName("setTotalPrice e getTotalPrice funcionam (@Transient — não persistido)")
    void deveSetarEObterTotalPrice() {
        System.out.println("[DEBUG] Testando totalPrice (@Transient)...");

        OrderEntity order = new OrderEntity();
        order.setTotalPrice(1598.0);

        assertEquals(1598.0, order.getTotalPrice(), 0.001);
        System.out.println("[DEBUG] ✓ totalPrice: " + order.getTotalPrice());
    }

    @Test
    @DisplayName("setTotalConvertedPrice e getTotalConvertedPrice funcionam (@Transient — não persistido)")
    void deveSetarEObterTotalConvertedPrice() {
        System.out.println("[DEBUG] Testando totalConvertedPrice (@Transient)...");

        OrderEntity order = new OrderEntity();
        order.setTotalConvertedPrice(9188.50);

        assertEquals(9188.50, order.getTotalConvertedPrice(), 0.001);
        System.out.println("[DEBUG] ✓ totalConvertedPrice: " + order.getTotalConvertedPrice());
    }
}