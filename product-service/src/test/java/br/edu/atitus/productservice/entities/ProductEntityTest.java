package br.edu.atitus.productservice.entities;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ProductEntity - Testes Unitários")
class ProductEntityTest {

    private ProductEntity entity;

    @BeforeEach
    void setUp() {
        System.out.println("[DEBUG] Criando instância de ProductEntity...");
        entity = new ProductEntity();
    }

    @Test
    @DisplayName("Deve instanciar com todos os campos nulos por padrão")
    void deveInstanciarComCamposNulos() {
        assertNotNull(entity);
        assertNull(entity.getId());
        assertNull(entity.getDescription());
        assertNull(entity.getBrand());
        assertNull(entity.getModel());
        assertNull(entity.getCurrency());
        assertNull(entity.getPrice());
        assertNull(entity.getStock());
        assertNull(entity.getImageURL());
        System.out.println("[DEBUG] ✓ Todos os campos nulos por padrão");
    }

    @Test
    @DisplayName("setId e getId devem funcionar corretamente")
    void setGetId_DeveFuncionar() {
        System.out.println("[DEBUG] Testando setId/getId...");
        entity.setId(42L);

        assertEquals(42L, entity.getId());
        System.out.println("[DEBUG] ✓ id: " + entity.getId());
    }

    @Test
    @DisplayName("setDescription e getDescription devem funcionar corretamente")
    void setGetDescription_DeveFuncionar() {
        System.out.println("[DEBUG] Testando setDescription/getDescription...");
        entity.setDescription("iPhone 15 128GB");

        assertEquals("iPhone 15 128GB", entity.getDescription());
        System.out.println("[DEBUG] ✓ description: " + entity.getDescription());
    }

    @Test
    @DisplayName("setBrand e getBrand, setModel e getModel devem funcionar")
    void setGetBrandModel_DeveFuncionar() {
        System.out.println("[DEBUG] Testando brand e model...");
        entity.setBrand("Apple");
        entity.setModel("iPhone 15");

        assertEquals("Apple", entity.getBrand());
        assertEquals("iPhone 15", entity.getModel());
        System.out.println("[DEBUG] ✓ brand=" + entity.getBrand() + " | model=" + entity.getModel());
    }

    @Test
    @DisplayName("setCurrency e getCurrency, setPrice e getPrice devem funcionar")
    void setGetCurrencyPrice_DeveFuncionar() {
        System.out.println("[DEBUG] Testando currency e price...");
        entity.setCurrency("USD");
        entity.setPrice(799.99);

        assertEquals("USD", entity.getCurrency());
        assertEquals(799.99, entity.getPrice(), 0.001);
        System.out.println("[DEBUG] ✓ currency=" + entity.getCurrency() + " | price=" + entity.getPrice());
    }

    @Test
    @DisplayName("setStock e getStock devem funcionar corretamente")
    void setGetStock_DeveFuncionar() {
        System.out.println("[DEBUG] Testando setStock/getStock...");
        entity.setStock(10);

        assertEquals(10, entity.getStock());
        System.out.println("[DEBUG] ✓ stock: " + entity.getStock());
    }

    @Test
    @DisplayName("setImageURL e getImageURL devem funcionar corretamente")
    void setGetImageURL_DeveFuncionar() {
        System.out.println("[DEBUG] Testando setImageURL/getImageURL...");
        String url = "https://example.com/iphone.jpg";
        entity.setImageURL(url);

        assertEquals(url, entity.getImageURL());
        System.out.println("[DEBUG] ✓ imageURL: " + entity.getImageURL());
    }

    @Test
    @DisplayName("Deve armazenar todos os campos simultaneamente sem interferência")
    void todosOsCampos_DevemSerIndependentes() {
        System.out.println("[DEBUG] Testando todos os campos juntos...");
        entity.setId(1L);
        entity.setDescription("Galaxy S24");
        entity.setBrand("Samsung");
        entity.setModel("Galaxy S24");
        entity.setCurrency("USD");
        entity.setPrice(859.00);
        entity.setStock(12);
        entity.setImageURL("https://example.com/s24.jpg");

        assertEquals(1L, entity.getId());
        assertEquals("Galaxy S24", entity.getDescription());
        assertEquals("Samsung", entity.getBrand());
        assertEquals("Galaxy S24", entity.getModel());
        assertEquals("USD", entity.getCurrency());
        assertEquals(859.00, entity.getPrice(), 0.001);
        assertEquals(12, entity.getStock());
        assertEquals("https://example.com/s24.jpg", entity.getImageURL());
        System.out.println("[DEBUG] ✓ Todos os campos independentes e corretos");
    }
}