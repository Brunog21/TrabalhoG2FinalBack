package br.edu.atitus.order_service.clients;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ProductResponse - Testes Unitários (record)")
class ProductResponseTest {

    private static final ProductResponse PRODUCT = new ProductResponse(
            1L,
            "iPhone 15 128GB",
            "Apple",
            "iPhone 15",
            799.0,
            "USD",
            15,
            "https://example.com/iphone.jpg",
            "product-port-8001",
            4594.25
    );

    @Test
    @DisplayName("Record criado com todos os campos corretos")
    void deveConstruirComTodosOsCampos() {
        System.out.println("[DEBUG] Testando construção de ProductResponse record...");

        assertNotNull(PRODUCT);
        System.out.println("[DEBUG] ✓ ProductResponse: " + PRODUCT);
    }

    @Test
    @DisplayName("id e description retornam valores corretos")
    void deveRetornarIdEDescription() {
        System.out.println("[DEBUG] Testando id e description...");

        assertEquals(1L, PRODUCT.id());
        assertEquals("iPhone 15 128GB", PRODUCT.description());

        System.out.println("[DEBUG] ✓ id=" + PRODUCT.id() + " | description=" + PRODUCT.description());
    }

    @Test
    @DisplayName("brand, model e price retornam valores corretos")
    void deveRetornarBrandModelEPrice() {
        System.out.println("[DEBUG] Testando brand, model e price...");

        assertEquals("Apple",    PRODUCT.brand());
        assertEquals("iPhone 15", PRODUCT.model());
        assertEquals(799.0,      PRODUCT.price(), 0.001);

        System.out.println("[DEBUG] ✓ brand=" + PRODUCT.brand()
                + " | model=" + PRODUCT.model()
                + " | price=" + PRODUCT.price());
    }

    @Test
    @DisplayName("currency, stock e imageURL retornam valores corretos")
    void deveRetornarCurrencyStockEImageURL() {
        System.out.println("[DEBUG] Testando currency, stock e imageURL...");

        assertEquals("USD", PRODUCT.currency());
        assertEquals(15,    PRODUCT.stock());
        assertEquals("https://example.com/iphone.jpg", PRODUCT.imageURL());

        System.out.println("[DEBUG] ✓ currency=" + PRODUCT.currency()
                + " | stock=" + PRODUCT.stock());
    }

    @Test
    @DisplayName("environment e convertedPrice retornam valores corretos")
    void deveRetornarEnvironmentEConvertedPrice() {
        System.out.println("[DEBUG] Testando environment e convertedPrice...");

        assertEquals("product-port-8001", PRODUCT.environment());
        assertEquals(4594.25, PRODUCT.convertedPrice(), 0.001);

        System.out.println("[DEBUG] ✓ environment=" + PRODUCT.environment()
                + " | convertedPrice=" + PRODUCT.convertedPrice());
    }
}