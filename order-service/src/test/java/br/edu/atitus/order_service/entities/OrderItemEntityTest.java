package br.edu.atitus.order_service.entities;

import br.edu.atitus.order_service.clients.ProductResponse;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("OrderItemEntity - Testes Unitários")
class OrderItemEntityTest {

    @Test
    @DisplayName("Instância com todos os campos nulos/zero por padrão")
    void deveInstanciarComValoresPadrao() {
        System.out.println("[DEBUG] Criando instância de OrderItemEntity...");

        OrderItemEntity item = new OrderItemEntity();

        assertNotNull(item);
        assertNull(item.getId(),                 "id deve ser null");
        assertNull(item.getProductId(),          "productId deve ser null");
        assertNull(item.getQuantity(),           "quantity deve ser null");
        assertEquals(0.0, item.getPriceAtPurchase(),         0.001, "priceAtPurchase default 0.0");
        assertNull(item.getCurrencyAtPurchase(), "currencyAtPurchase deve ser null");
        assertNull(item.getOrder(),              "order deve ser null (@JsonIgnore)");
        assertNull(item.getProduct(),            "product deve ser null (@Transient)");
        assertEquals(0.0, item.getConvertedPriceAtPruchase(), 0.001,
                "convertedPriceAtPruchase default 0.0 (@Transient, typo preservado)");

        System.out.println("[DEBUG] ✓ Todos os campos nulos/zero por padrão");
    }

    @Test
    @DisplayName("setId e getId funcionam")
    void deveSetarEObterI() {
        System.out.println("[DEBUG] Testando setId/getId...");

        OrderItemEntity item = new OrderItemEntity();
        item.setId(5L);

        assertEquals(5L, item.getId());
        System.out.println("[DEBUG] ✓ id: " + item.getId());
    }

    @Test
    @DisplayName("setProductId e getProductId funcionam")
    void deveSetarEObterProductId() {
        System.out.println("[DEBUG] Testando setProductId/getProductId...");

        OrderItemEntity item = new OrderItemEntity();
        item.setProductId(100L);

        assertEquals(100L, item.getProductId());
        System.out.println("[DEBUG] ✓ productId: " + item.getProductId());
    }

    @Test
    @DisplayName("setQuantity e getQuantity funcionam")
    void deveSetarEObterQuantity() {
        System.out.println("[DEBUG] Testando setQuantity/getQuantity...");

        OrderItemEntity item = new OrderItemEntity();
        item.setQuantity(3);

        assertEquals(3, item.getQuantity());
        System.out.println("[DEBUG] ✓ quantity: " + item.getQuantity());
    }

    @Test
    @DisplayName("setPriceAtPurchase e setCurrencyAtPurchase funcionam")
    void deveSetarPrecoEMoedaDaCompra() {
        System.out.println("[DEBUG] Testando priceAtPurchase e currencyAtPurchase...");

        OrderItemEntity item = new OrderItemEntity();
        item.setPriceAtPurchase(799.0);
        item.setCurrencyAtPurchase("USD");

        assertEquals(799.0, item.getPriceAtPurchase(), 0.001);
        assertEquals("USD", item.getCurrencyAtPurchase());

        System.out.println("[DEBUG] ✓ priceAtPurchase=799.0 | currencyAtPurchase=USD");
    }

    @Test
    @DisplayName("setProduct e getProduct funcionam (@Transient — preenchido via Feign em runtime)")
    void deveSetarEObterProduct() {
        System.out.println("[DEBUG] Testando campo product (@Transient)...");

        OrderItemEntity item = new OrderItemEntity();
        ProductResponse product = new ProductResponse(
                1L, "iPhone 15 128GB", "Apple", "iPhone 15",
                799.0, "USD", 10,
                "https://example.com/iphone.jpg", "product-port-8001", 0.0
        );

        item.setProduct(product);

        assertNotNull(item.getProduct());
        assertEquals(1L,       item.getProduct().id());
        assertEquals("Apple",  item.getProduct().brand());
        assertEquals("iPhone 15 128GB", item.getProduct().description());

        System.out.println("[DEBUG] ✓ product.brand=" + item.getProduct().brand()
                + " | product.price=" + item.getProduct().price());
    }

    @Test
    @DisplayName("setConvertedPriceAtPruchase e getConvertedPriceAtPruchase funcionam (typo preservado)")
    void deveSetarEObterConvertedPriceAtPruchase() {
        System.out.println("[DEBUG] Testando convertedPriceAtPruchase (typo 'Pruchase' do código de produção)...");

        OrderItemEntity item = new OrderItemEntity();
        item.setConvertedPriceAtPruchase(4594.25);

        assertEquals(4594.25, item.getConvertedPriceAtPruchase(), 0.001);
        System.out.println("[DEBUG] ✓ convertedPriceAtPruchase: " + item.getConvertedPriceAtPruchase());
    }
}