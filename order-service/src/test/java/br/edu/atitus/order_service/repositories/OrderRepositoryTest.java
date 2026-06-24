package br.edu.atitus.order_service.repositories;

import br.edu.atitus.order_service.entities.OrderEntity;
import br.edu.atitus.order_service.entities.OrderItemEntity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("OrderRepository - Testes de Integração com H2")
class OrderRepositoryTest {

    @Autowired
    private OrderRepository repository;

    @BeforeEach
    void setup() {
        System.out.println("[DEBUG] ===== Limpando banco H2 =====");
        repository.deleteAll();
    }

    private OrderEntity salvarOrder(Long customerId) {
        OrderEntity order = new OrderEntity();
        order.setCustomerId(customerId);
        order.setOrderDate(LocalDateTime.of(2026, 6, 22, 10, 0, 0));
        order.setItems(new ArrayList<>());
        return repository.save(order);
    }

    private OrderEntity salvarOrderComItem(Long customerId, Long productId,
                                           double price, String currency, int qty) {
        OrderEntity order = new OrderEntity();
        order.setCustomerId(customerId);
        order.setOrderDate(LocalDateTime.of(2026, 6, 22, 10, 0, 0));

        OrderItemEntity item = new OrderItemEntity();
        item.setProductId(productId);
        item.setQuantity(qty);
        item.setPriceAtPurchase(price);
        item.setCurrencyAtPurchase(currency);
        item.setOrder(order);

        order.setItems(List.of(item));
        return repository.save(order);
    }

    @Test
    @DisplayName("save persiste OrderEntity e findById recupera por ID")
    void deveSalvarEBuscarPorId() {
        System.out.println("[DEBUG] Testando save e findById...");

        OrderEntity saved = salvarOrder(1L);

        assertNotNull(saved.getId(), "ID deve ser gerado");
        Optional<OrderEntity> found = repository.findById(saved.getId());
        assertTrue(found.isPresent(), "Deve encontrar a order salva");
        assertEquals(1L, found.get().getCustomerId());

        System.out.println("[DEBUG] ✓ Order salva com ID=" + saved.getId());
    }

    @Test
    @DisplayName("findByCustomerId retorna somente pedidos do cliente informado")
    void deveBuscarPorCustomerId() {
        System.out.println("[DEBUG] Testando findByCustomerId — cliente correto...");

        salvarOrder(1L);
        salvarOrder(2L);

        Pageable pageable = PageRequest.of(0, 10);
        Page<OrderEntity> result = repository.findByCustomerId(1L, pageable);

        assertEquals(1, result.getTotalElements(),
                "Deve retornar apenas 1 order para customerId=1");
        assertEquals(1L, result.getContent().get(0).getCustomerId());

        System.out.println("[DEBUG] ✓ findByCustomerId retornou " + result.getTotalElements() + " order(s) para customerId=1");
    }

    @Test
    @DisplayName("findByCustomerId não retorna pedidos de outros clientes")
    void naoDeveRetornarPedidosDeOutroCliente() {
        System.out.println("[DEBUG] Testando isolamento entre clientes...");

        salvarOrder(1L);
        salvarOrder(1L);
        salvarOrder(3L); // outro cliente

        Pageable pageable = PageRequest.of(0, 10);
        Page<OrderEntity> resultCliente1 = repository.findByCustomerId(1L, pageable);
        Page<OrderEntity> resultCliente3 = repository.findByCustomerId(3L, pageable);
        Page<OrderEntity> resultCliente99 = repository.findByCustomerId(99L, pageable);

        assertEquals(2, resultCliente1.getTotalElements(),  "Cliente 1 tem 2 orders");
        assertEquals(1, resultCliente3.getTotalElements(),  "Cliente 3 tem 1 order");
        assertEquals(0, resultCliente99.getTotalElements(), "Cliente 99 não tem orders");

        System.out.println("[DEBUG] ✓ Cliente 1: " + resultCliente1.getTotalElements()
                + " | Cliente 3: " + resultCliente3.getTotalElements()
                + " | Cliente 99: " + resultCliente99.getTotalElements());
    }

    @Test
    @DisplayName("findAll retorna todas as orders persistidas")
    void deveRetornarTodasAsOrders() {
        System.out.println("[DEBUG] Testando findAll com 3 orders...");

        salvarOrder(1L);
        salvarOrder(2L);
        salvarOrder(3L);

        List<OrderEntity> all = repository.findAll();

        assertEquals(3, all.size(), "findAll deve retornar 3 orders");
        System.out.println("[DEBUG] ✓ findAll retornou " + all.size() + " orders");
    }

    @Test
    @DisplayName("count retorna total correto de orders")
    void deveRetornarCountCorreto() {
        System.out.println("[DEBUG] Testando count...");

        salvarOrder(1L);
        salvarOrder(2L);

        long count = repository.count();

        assertEquals(2L, count, "count deve ser 2");
        System.out.println("[DEBUG] ✓ count retornou " + count);
    }

    @Test
    @DisplayName("delete remove OrderEntity e findById retorna Optional vazio")
    void deveDeletarOrder() {
        System.out.println("[DEBUG] Testando delete...");

        OrderEntity saved = salvarOrder(1L);
        Long id = saved.getId();

        repository.deleteById(id);
        Optional<OrderEntity> found = repository.findById(id);

        assertFalse(found.isPresent(), "Não deve encontrar order deletada");
        System.out.println("[DEBUG] ✓ Order ID=" + id + " deletada com sucesso");
    }

    @Test
    @DisplayName("findByCustomerId recupera order com items persistidos via CascadeType.ALL")
    void deveBuscarOrderComItemsPersistidos() {
        System.out.println("[DEBUG] Testando findByCustomerId com items (CascadeType.ALL)...");

        salvarOrderComItem(1L, 10L, 799.0, "USD", 2);

        Pageable pageable = PageRequest.of(0, 10);
        Page<OrderEntity> result = repository.findByCustomerId(1L, pageable);

        assertEquals(1, result.getTotalElements());

        assertEquals(1L, result.getContent().get(0).getCustomerId());

        System.out.println("[DEBUG] ✓ Order do customerId=1 recuperada com CascadeType.ALL");
    }
}