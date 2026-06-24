package br.edu.atitus.productservice.repositories;

import br.edu.atitus.productservice.entities.ProductEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cache.CacheManager;
import org.springframework.cache.support.NoOpCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("ProductRepository - Testes de Integração com H2")
class ProductRepositoryTest {

    /**
     * @EnableCaching na ProductServiceApplication exige CacheManager.
     * @DataJpaTest não inclui CacheAutoConfiguration.
     * Solução: NoOpCacheManager via @TestConfiguration inner class.
     */
    @TestConfiguration
    static class TestCacheConfig {
        @Bean
        CacheManager cacheManager() {
            System.out.println("[DEBUG] NoOpCacheManager registrado para satisfazer @EnableCaching no slice JPA");
            return new NoOpCacheManager();
        }
    }

    @Autowired
    private ProductRepository productRepository;

    private ProductEntity buildEntity(String description, String brand, String currency, Double price) {
        ProductEntity e = new ProductEntity();
        e.setDescription(description);
        e.setBrand(brand);
        e.setModel(brand + " Model");
        e.setCurrency(currency);
        e.setPrice(price);
        e.setStock(10);
        e.setImageURL("https://example.com/img.jpg");
        return e;
    }

    @BeforeEach
    void setUp() {
        System.out.println("[DEBUG] ===== Limpando banco H2 =====");
        productRepository.deleteAll();
    }

    @Test
    @DisplayName("Deve salvar nova entidade e gerar ID automaticamente")
    void save_NovaEntidade_DeveGerarId() {
        System.out.println("[DEBUG] Testando save de nova entidade...");
        ProductEntity entity = buildEntity("iPhone 15 128GB", "Apple", "USD", 799.00);

        ProductEntity saved = productRepository.save(entity);

        assertNotNull(saved.getId());
        assertTrue(saved.getId() > 0);
        assertEquals("iPhone 15 128GB", saved.getDescription());
        assertEquals(799.00, saved.getPrice(), 0.001);
        System.out.println("[DEBUG] ✓ Entidade salva com ID=" + saved.getId());
    }

    @Test
    @DisplayName("findById deve retornar entidade existente")
    void findById_EntidadeExistente_DeveRetornar() {
        System.out.println("[DEBUG] Testando findById com entidade existente...");
        ProductEntity entity = productRepository.save(buildEntity("Galaxy S24", "Samsung", "USD", 859.00));

        Optional<ProductEntity> result = productRepository.findById(entity.getId());

        assertTrue(result.isPresent());
        assertEquals("Galaxy S24", result.get().getDescription());
        assertEquals("Samsung", result.get().getBrand());
        System.out.println("[DEBUG] ✓ Entidade encontrada: ID=" + result.get().getId());
    }

    @Test
    @DisplayName("findById deve retornar Optional vazio para ID inexistente")
    void findById_IdInexistente_DeveRetornarVazio() {
        System.out.println("[DEBUG] Testando findById com ID inexistente...");

        Optional<ProductEntity> result = productRepository.findById(9999L);

        assertFalse(result.isPresent());
        System.out.println("[DEBUG] ✓ Optional vazio para ID 9999");
    }

    @Test
    @DisplayName("findAll deve retornar todas as entidades salvas")
    void findAll_DeveRetornarTodasEntidades() {
        System.out.println("[DEBUG] Testando findAll com 3 produtos...");
        productRepository.save(buildEntity("iPhone 15", "Apple", "USD", 799.00));
        productRepository.save(buildEntity("Galaxy S24", "Samsung", "USD", 859.00));
        productRepository.save(buildEntity("Pixel 8", "Google", "USD", 699.00));

        List<ProductEntity> all = productRepository.findAll();

        assertEquals(3, all.size());
        System.out.println("[DEBUG] ✓ findAll retornou " + all.size() + " entidades");
    }

    @Test
    @DisplayName("count deve retornar o total correto de registros")
    void count_DeveRetornarTotalCorreto() {
        System.out.println("[DEBUG] Testando count...");
        productRepository.save(buildEntity("iPhone 15", "Apple", "USD", 799.00));
        productRepository.save(buildEntity("Galaxy S24", "Samsung", "USD", 859.00));

        assertEquals(2L, productRepository.count());
        System.out.println("[DEBUG] ✓ count retornou 2");
    }

    @Test
    @DisplayName("delete deve remover entidade e não encontrá-la depois")
    void delete_EntidadeExistente_DeveRemover() {
        System.out.println("[DEBUG] Testando delete...");
        ProductEntity entity = productRepository.save(buildEntity("Moto G84", "Motorola", "USD", 299.00));
        Long id = entity.getId();

        productRepository.delete(entity);

        assertFalse(productRepository.findById(id).isPresent());
        System.out.println("[DEBUG] ✓ Entidade ID=" + id + " removida com sucesso");
    }

    @Test
    @DisplayName("save deve atualizar campos de entidade existente")
    void save_EntidadeExistente_DeveAtualizarCampos() {
        System.out.println("[DEBUG] Testando update via save...");
        ProductEntity entity = productRepository.save(buildEntity("Moto G84", "Motorola", "USD", 299.00));

        entity.setPrice(249.00);
        entity.setStock(5);
        ProductEntity updated = productRepository.save(entity);

        assertEquals(249.00, updated.getPrice(), 0.001);
        assertEquals(5, updated.getStock());
        assertEquals(entity.getId(), updated.getId()); // mesmo ID
        System.out.println("[DEBUG] ✓ Preço atualizado para " + updated.getPrice());
    }

    @Test
    @DisplayName("Deve salvar e recuperar corretamente todos os campos da entidade")
    void save_DevePreservarTodosOsCampos() {
        System.out.println("[DEBUG] Testando persistência de todos os campos...");
        ProductEntity entity = new ProductEntity();
        entity.setDescription("OnePlus 12 256GB");
        entity.setBrand("OnePlus");
        entity.setModel("OnePlus 12");
        entity.setCurrency("USD");
        entity.setPrice(799.00);
        entity.setStock(11);
        entity.setImageURL("https://example.com/oneplus12.jpg");

        ProductEntity saved = productRepository.save(entity);
        Optional<ProductEntity> found = productRepository.findById(saved.getId());

        assertTrue(found.isPresent());
        assertEquals("OnePlus 12 256GB", found.get().getDescription());
        assertEquals("OnePlus", found.get().getBrand());
        assertEquals("OnePlus 12", found.get().getModel());
        assertEquals("USD", found.get().getCurrency());
        assertEquals(799.00, found.get().getPrice(), 0.001);
        assertEquals(11, found.get().getStock());
        assertEquals("https://example.com/oneplus12.jpg", found.get().getImageURL());
        System.out.println("[DEBUG] ✓ Todos os campos preservados corretamente");
    }
}