package br.edu.atitus.currencyservice.repositories;

import br.edu.atitus.currencyservice.entities.CurrencyEntity;
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
@DisplayName("CurrencyRepository - Testes de Integração com H2")
class CurrencyRepositoryTest {

    /**
     * O @EnableCaching da CurrencyServiceApplication registra o CacheAspectSupport,
     * que exige um CacheManager bean durante afterSingletonsInstantiated().
     * O slice @DataJpaTest não inclui CacheAutoConfiguration, então precisamos
     * fornecer um NoOpCacheManager diretamente via @TestConfiguration.
     *
     * Solução: inner class @TestConfiguration — automaticamente detectada pelo @DataJpaTest.
     * NoOpCacheManager: implementação vazia que não faz cache de nada (apenas satisfaz a dependência).
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
    private CurrencyRepository currencyRepository;

    @BeforeEach
    void setUp() {
        System.out.println("[DEBUG] ===== Limpando e populando banco H2 =====");
        currencyRepository.deleteAll();

        CurrencyEntity usdBrl = new CurrencyEntity();
        usdBrl.setSourceCurrency("USD");
        usdBrl.setTargetCurrency("BRL");
        usdBrl.setConversionRate(5.00);
        currencyRepository.save(usdBrl);

        CurrencyEntity eurBrl = new CurrencyEntity();
        eurBrl.setSourceCurrency("EUR");
        eurBrl.setTargetCurrency("BRL");
        eurBrl.setConversionRate(6.00);
        currencyRepository.save(eurBrl);

        System.out.println("[DEBUG] Inseridos: USD/BRL=5.00, EUR/BRL=6.00");
    }

    @Test
    @DisplayName("Deve encontrar entidade USD/BRL que existe no banco")
    void findBySourceCurrencyAndTargetCurrency_USD_BRL_DeveRetornarEntidade() {
        Optional<CurrencyEntity> result = currencyRepository
                .findBySourceCurrencyAndTargetCurrency("USD", "BRL");

        assertTrue(result.isPresent(), "Optional deve estar presente para USD/BRL");
        assertNotNull(result.get().getId(), "ID deve ser gerado pelo banco");
        assertEquals("USD", result.get().getSourceCurrency());
        assertEquals("BRL", result.get().getTargetCurrency());
        assertEquals(5.00, result.get().getConversionRate(), 0.001);

        System.out.println("[DEBUG] ✓ Encontrado: ID=" + result.get().getId());
    }

    @Test
    @DisplayName("Deve encontrar entidade EUR/BRL que existe no banco")
    void findBySourceCurrencyAndTargetCurrency_EUR_BRL_DeveRetornarEntidade() {
        Optional<CurrencyEntity> result = currencyRepository
                .findBySourceCurrencyAndTargetCurrency("EUR", "BRL");

        assertTrue(result.isPresent());
        assertEquals("EUR", result.get().getSourceCurrency());
        assertEquals(6.00, result.get().getConversionRate(), 0.001);
        System.out.println("[DEBUG] ✓ EUR/BRL encontrado");
    }

    @Test
    @DisplayName("Deve retornar Optional vazio para moeda GBP/BRL que não existe")
    void findBySourceCurrencyAndTargetCurrency_GBP_BRL_NaoExiste_DeveRetornarVazio() {
        Optional<CurrencyEntity> result = currencyRepository
                .findBySourceCurrencyAndTargetCurrency("GBP", "BRL");

        assertFalse(result.isPresent());
        System.out.println("[DEBUG] ✓ Optional vazio para GBP/BRL");
    }

    @Test
    @DisplayName("Deve retornar Optional vazio quando source está incorreto")
    void findBySourceCurrencyAndTargetCurrency_SourceErrado_DeveRetornarVazio() {
        Optional<CurrencyEntity> result = currencyRepository
                .findBySourceCurrencyAndTargetCurrency("WRONG", "BRL");

        assertFalse(result.isPresent());
        System.out.println("[DEBUG] ✓ Optional vazio para source inválido");
    }

    @Test
    @DisplayName("Deve retornar Optional vazio quando target está incorreto")
    void findBySourceCurrencyAndTargetCurrency_TargetErrado_DeveRetornarVazio() {
        Optional<CurrencyEntity> result = currencyRepository
                .findBySourceCurrencyAndTargetCurrency("USD", "WRONG");

        assertFalse(result.isPresent());
        System.out.println("[DEBUG] ✓ Optional vazio para target inválido");
    }

    @Test
    @DisplayName("Deve retornar Optional vazio quando ambos os parâmetros estão incorretos")
    void findBySourceCurrencyAndTargetCurrency_AmbosErrados_DeveRetornarVazio() {
        Optional<CurrencyEntity> result = currencyRepository
                .findBySourceCurrencyAndTargetCurrency("XXX", "YYY");

        assertFalse(result.isPresent());
        System.out.println("[DEBUG] ✓ Optional vazio para XXX/YYY");
    }

    @Test
    @DisplayName("Deve salvar nova entidade GBP/BRL e gerar ID")
    void save_NovaEntidade_DeveGerarIdEPersistir() {
        CurrencyEntity entity = new CurrencyEntity();
        entity.setSourceCurrency("GBP");
        entity.setTargetCurrency("BRL");
        entity.setConversionRate(7.50);

        CurrencyEntity saved = currencyRepository.save(entity);

        assertNotNull(saved.getId());
        assertTrue(saved.getId() > 0);
        assertEquals("GBP", saved.getSourceCurrency());
        assertEquals(7.50, saved.getConversionRate(), 0.001);

        Optional<CurrencyEntity> found = currencyRepository
                .findBySourceCurrencyAndTargetCurrency("GBP", "BRL");
        assertTrue(found.isPresent());
        System.out.println("[DEBUG] ✓ GBP/BRL salvo com ID=" + saved.getId());
    }

    @Test
    @DisplayName("Deve atualizar taxa de conversão de USD/BRL")
    void save_AtualizaTaxa_DeveRefletirNovoValor() {
        CurrencyEntity entity = currencyRepository
                .findBySourceCurrencyAndTargetCurrency("USD", "BRL")
                .orElseThrow();

        entity.setConversionRate(5.99);
        CurrencyEntity updated = currencyRepository.save(entity);

        assertEquals(5.99, updated.getConversionRate(), 0.001);
        System.out.println("[DEBUG] ✓ Taxa atualizada para: " + updated.getConversionRate());
    }

    @Test
    @DisplayName("Deve deletar entidade USD/BRL e não encontrá-la depois")
    void delete_EntidadeExistente_DeveRemoverDoBanco() {
        CurrencyEntity entity = currencyRepository
                .findBySourceCurrencyAndTargetCurrency("USD", "BRL")
                .orElseThrow();

        Long deletedId = entity.getId();
        currencyRepository.delete(entity);

        assertFalse(currencyRepository
                .findBySourceCurrencyAndTargetCurrency("USD", "BRL").isPresent());
        assertFalse(currencyRepository.findById(deletedId).isPresent());
        System.out.println("[DEBUG] ✓ USD/BRL deletado com sucesso");
    }

    @Test
    @DisplayName("findAll deve retornar as 2 entidades inseridas no setUp")
    void findAll_DeveRetornar2Entidades() {
        List<CurrencyEntity> all = currencyRepository.findAll();

        assertEquals(2, all.size());
        System.out.println("[DEBUG] ✓ findAll retornou " + all.size() + " entidades");
    }

    @Test
    @DisplayName("count deve retornar 2 após o setUp")
    void count_DeveRetornar2() {
        assertEquals(2L, currencyRepository.count());
        System.out.println("[DEBUG] ✓ count retornou 2");
    }

    @Test
    @DisplayName("Deve recuperar entidade USD/BRL pelo ID")
    void findById_EntidadeExistente_DeveRetornarEntidade() {
        Long id = currencyRepository
                .findBySourceCurrencyAndTargetCurrency("USD", "BRL")
                .orElseThrow().getId();

        Optional<CurrencyEntity> found = currencyRepository.findById(id);

        assertTrue(found.isPresent());
        assertEquals("USD", found.get().getSourceCurrency());
        System.out.println("[DEBUG] ✓ Entidade encontrada pelo ID=" + id);
    }
}