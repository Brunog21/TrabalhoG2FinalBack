package br.edu.atitus.order_service.services;

import br.edu.atitus.order_service.clients.CurrencyClient;
import br.edu.atitus.order_service.clients.CurrencyResponse;
import br.edu.atitus.order_service.clients.ProductClient;
import br.edu.atitus.order_service.clients.ProductResponse;
import br.edu.atitus.order_service.entities.OrderEntity;
import br.edu.atitus.order_service.entities.OrderItemEntity;
import br.edu.atitus.order_service.repositories.OrderRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrderService - Testes Unitários (Mockito)")
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ProductClient productClient;

    @Mock
    private CurrencyClient currencyClient;

    @InjectMocks
    private OrderService orderService;

    private OrderEntity order;
    private OrderItemEntity item;
    private ProductResponse productResponse;
    private CurrencyResponse currencyResponse;
    private Pageable pageable;

    @BeforeEach
    void setUp() {
        System.out.println("[DEBUG] ===== Setup do teste iniciado =====");

        pageable = PageRequest.of(0, 5, Sort.by("orderDate").ascending());

        item = new OrderItemEntity();
        item.setProductId(1L);
        item.setQuantity(2);
        item.setPriceAtPurchase(100.0);
        item.setCurrencyAtPurchase("USD");

        order = new OrderEntity();
        order.setId(1L);
        order.setCustomerId(1L);
        order.setOrderDate(LocalDateTime.of(2026, 6, 22, 10, 0, 0));
        order.setItems(new ArrayList<>(List.of(item)));

        productResponse = new ProductResponse(
                1L, "iPhone 15 128GB", "Apple", "iPhone 15",
                100.0, "USD", 10,
                "https://example.com/iphone.jpg", "product-port-8001", 0.0
        );

        currencyResponse = new CurrencyResponse();
        currencyResponse.setSourceCurrency("USD");
        currencyResponse.setTargetCurrency("BRL");
        currencyResponse.setConversionRate(5.75);

        System.out.println("[DEBUG] item configurado: productId=1 | qty=2 | price=100.0 | currency=USD");
    }

    @Test
    @DisplayName("createOrder debita estoque, salva pedido e não restaura em caso de sucesso")
    void createOrder_DeveDelegarParaRepository() {
        System.out.println("[DEBUG] === Teste 1: createOrder delega ao repository ===");

        OrderEntity orderASalvar = new OrderEntity();
        orderASalvar.setCustomerId(1L);
        orderASalvar.setOrderDate(LocalDateTime.now());
        orderASalvar.setItems(new ArrayList<>(List.of(item)));

        OrderEntity orderSalva = new OrderEntity();
        orderSalva.setId(10L);
        orderSalva.setCustomerId(1L);

        when(orderRepository.save(any(OrderEntity.class))).thenReturn(orderSalva);

        OrderEntity result = orderService.createOrder(orderASalvar, 1L);

        assertNotNull(result);
        assertEquals(10L, result.getId());
        verify(productClient, times(1)).debitStock(any());
        verify(orderRepository, times(1)).save(orderASalvar);
        verify(productClient, never()).restoreStock(any());
        verifyNoInteractions(currencyClient);

        System.out.println("[DEBUG] ✓ createOrder delegou para repository.save | ID=" + result.getId());
    }

    @Test
    @DisplayName("findOrdersByCustomerId retorna página de orders do cliente")
    void findOrdersByCustomerId_DeveRetornarPagina() {
        System.out.println("[DEBUG] === Teste 2: findOrdersByCustomerId retorna página ===");

        Page<OrderEntity> page = new PageImpl<>(List.of(order), pageable, 1);
        when(orderRepository.findByCustomerId(1L, pageable)).thenReturn(page);
        when(productClient.getProductById(1L)).thenReturn(productResponse);
        when(currencyClient.getCurrency("USD", "BRL")).thenReturn(currencyResponse);

        Page<OrderEntity> result = orderService.findOrdersByCustomerId(1L, "BRL", pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(orderRepository, times(1)).findByCustomerId(1L, pageable);

        System.out.println("[DEBUG] ✓ findOrdersByCustomerId retornou " + result.getTotalElements() + " order(s)");
    }

    @Test
    @DisplayName("findOrdersByCustomerId calcula totalPrice corretamente (priceAtPurchase * quantity)")
    void findOrdersByCustomerId_DeveCalcularTotalPrice() {
        System.out.println("[DEBUG] === Teste 3: cálculo de totalPrice ===");

        Page<OrderEntity> page = new PageImpl<>(List.of(order), pageable, 1);
        when(orderRepository.findByCustomerId(1L, pageable)).thenReturn(page);
        when(productClient.getProductById(1L)).thenReturn(productResponse);
        when(currencyClient.getCurrency("USD", "BRL")).thenReturn(currencyResponse);

        Page<OrderEntity> result = orderService.findOrdersByCustomerId(1L, "BRL", pageable);
        OrderEntity orderResult = result.getContent().get(0);

        // 100.0 * 2 = 200.0
        assertEquals(200.0, orderResult.getTotalPrice(), 0.001,
                "totalPrice deve ser priceAtPurchase * quantity");

        System.out.println("[DEBUG] ✓ totalPrice=" + orderResult.getTotalPrice()
                + " (esperado: 200.0 = 100.0 * 2)");
    }

    @Test
    @DisplayName("findOrdersByCustomerId calcula totalConvertedPrice corretamente (price * rate * qty)")
    void findOrdersByCustomerId_DeveCalcularTotalConvertedPrice() {
        System.out.println("[DEBUG] === Teste 4: cálculo de totalConvertedPrice ===");

        Page<OrderEntity> page = new PageImpl<>(List.of(order), pageable, 1);
        when(orderRepository.findByCustomerId(1L, pageable)).thenReturn(page);
        when(productClient.getProductById(1L)).thenReturn(productResponse);
        when(currencyClient.getCurrency("USD", "BRL")).thenReturn(currencyResponse);

        Page<OrderEntity> result = orderService.findOrdersByCustomerId(1L, "BRL", pageable);
        OrderEntity orderResult = result.getContent().get(0);

        assertEquals(1150.0, orderResult.getTotalConvertedPrice(), 0.001,
                "totalConvertedPrice deve ser priceAtPurchase * conversionRate * quantity");

        assertEquals(575.0, item.getConvertedPriceAtPruchase(), 0.001,
                "convertedPriceAtPruchase do item = 100.0 * 5.75");

        System.out.println("[DEBUG] ✓ convertedPriceAtPruchase(item)=" + item.getConvertedPriceAtPruchase()
                + " | totalConvertedPrice=" + orderResult.getTotalConvertedPrice()
                + " (esperado: 575.0 | 1150.0)");
    }

    @Test
    @DisplayName("findOrdersByCustomerId chama ProductClient e CurrencyClient por item")
    void findOrdersByCustomerId_DeveEnriquecerItems() {
        System.out.println("[DEBUG] === Teste 5: enriquecimento de items ===");

        Page<OrderEntity> page = new PageImpl<>(List.of(order), pageable, 1);
        when(orderRepository.findByCustomerId(1L, pageable)).thenReturn(page);
        when(productClient.getProductById(1L)).thenReturn(productResponse);
        when(currencyClient.getCurrency("USD", "BRL")).thenReturn(currencyResponse);

        orderService.findOrdersByCustomerId(1L, "BRL", pageable);

        verify(productClient, times(1)).getProductById(1L);
        verify(currencyClient, times(1)).getCurrency("USD", "BRL");
        assertNotNull(item.getProduct(), "item.product deve ser preenchido pelo serviço");
        assertEquals("Apple", item.getProduct().brand());

        System.out.println("[DEBUG] ✓ ProductClient chamado 1x | CurrencyClient chamado 1x");
        System.out.println("[DEBUG] ✓ item.product.brand=" + item.getProduct().brand());
    }

    @Test
    @DisplayName("findOrdersByCustomerId não chama CurrencyClient quando moeda já é a de destino")
    void findOrdersByCustomerId_MesmaMoeda_NaoChamaCurrencyClient() {
        item.setCurrencyAtPurchase("BRL");

        Page<OrderEntity> page = new PageImpl<>(List.of(order), pageable, 1);
        when(orderRepository.findByCustomerId(1L, pageable)).thenReturn(page);
        when(productClient.getProductById(1L)).thenReturn(productResponse);

        Page<OrderEntity> result = orderService.findOrdersByCustomerId(1L, "BRL", pageable);
        OrderEntity orderResult = result.getContent().get(0);

        assertEquals(200.0, orderResult.getTotalPrice(), 0.001);
        assertEquals(200.0, orderResult.getTotalConvertedPrice(), 0.001);
        verify(currencyClient, never()).getCurrency(anyString(), anyString());
    }

    @Test
    @DisplayName("findOrdersByCustomerId continua quando produto não existe mais")
    void findOrdersByCustomerId_ProdutoIndisponivel_DeveUsarSnapshot() {
        Page<OrderEntity> page = new PageImpl<>(List.of(order), pageable, 1);
        when(orderRepository.findByCustomerId(1L, pageable)).thenReturn(page);
        when(productClient.getProductById(1L)).thenThrow(new RuntimeException("Produto não encontrado"));
        when(currencyClient.getCurrency("USD", "EUR")).thenReturn(currencyResponse);

        currencyResponse.setTargetCurrency("EUR");
        currencyResponse.setConversionRate(0.92);

        Page<OrderEntity> result = orderService.findOrdersByCustomerId(1L, "EUR", pageable);
        OrderEntity orderResult = result.getContent().get(0);

        assertNotNull(item.getProduct());
        assertEquals(1L, item.getProduct().id());
        assertEquals(184.0, orderResult.getTotalConvertedPrice(), 0.001);
    }

    @Test
    @DisplayName("findOrdersByCustomerId usa taxa 1.0 quando conversão falha")
    void findOrdersByCustomerId_ConversaoFalha_DeveManterPrecoOriginal() {
        item.setCurrencyAtPurchase("BRL");

        Page<OrderEntity> page = new PageImpl<>(List.of(order), pageable, 1);
        when(orderRepository.findByCustomerId(1L, pageable)).thenReturn(page);
        when(productClient.getProductById(1L)).thenReturn(productResponse);
        when(currencyClient.getCurrency("BRL", "EUR")).thenThrow(new RuntimeException("Currency not found"));

        Page<OrderEntity> result = orderService.findOrdersByCustomerId(1L, "EUR", pageable);
        OrderEntity orderResult = result.getContent().get(0);

        assertEquals(200.0, orderResult.getTotalConvertedPrice(), 0.001);
    }
}