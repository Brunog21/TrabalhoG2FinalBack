package br.edu.atitus.currencyservice.services;

import java.util.Locale;
import java.util.Optional;

import org.springframework.cache.CacheManager;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import br.edu.atitus.currencyservice.clients.BCBClient;
import br.edu.atitus.currencyservice.clients.BCBResponse;
import br.edu.atitus.currencyservice.entities.CurrencyEntity;
import br.edu.atitus.currencyservice.repositories.CurrencyRepository;

@Service
public class CurrencyConversionService {

    private static final String BCB_CACHE_NAME = "bcb-currency";
    private static final String BCB_QUOTE_DATE = "05-14-2026";
    private static final String HUB_CURRENCY = "USD";

    private final CurrencyRepository currencyRepository;
    private final BCBClient bcbClient;
    private final CacheManager cacheManager;
    private final Environment environment;

    public CurrencyConversionService(
            CurrencyRepository currencyRepository,
            BCBClient bcbClient,
            CacheManager cacheManager,
            Environment environment) {
        this.currencyRepository = currencyRepository;
        this.bcbClient = bcbClient;
        this.cacheManager = cacheManager;
        this.environment = environment;
    }

    public ConversionResult convert(String source, String target) {
        String normalizedSource = normalize(source);
        String normalizedTarget = normalize(target);
        double rate = resolveConversionRate(normalizedSource, normalizedTarget);
        String port = environment.getProperty("local.server.port");
        return new ConversionResult(normalizedSource, normalizedTarget, rate, "currency-service running on port: " + port);
    }

    private String normalize(String currency) {
        return currency.toUpperCase(Locale.ROOT);
    }

    private double resolveConversionRate(String source, String target) {
        if (source.equals(target)) {
            return 1.0;
        }

        Optional<CurrencyEntity> direct = currencyRepository.findBySourceCurrencyAndTargetCurrency(source, target);
        if (direct.isPresent()) {
            return resolveDirectRate(direct.get(), source, target);
        }

        Optional<CurrencyEntity> inverse = currencyRepository.findBySourceCurrencyAndTargetCurrency(target, source);
        if (inverse.isPresent()) {
            return 1.0 / resolveDirectRate(inverse.get(), target, source);
        }

        if (!HUB_CURRENCY.equals(source) && !HUB_CURRENCY.equals(target)) {
            return resolveConversionRate(source, HUB_CURRENCY) * resolveConversionRate(HUB_CURRENCY, target);
        }

        throw new RuntimeException("Currency not found");
    }

    private double resolveDirectRate(CurrencyEntity currencyEntity, String source, String target) {
        if ("BRL".equalsIgnoreCase(target)) {
            Double bcbRate = resolveBcbRate(source);
            if (bcbRate != null) {
                return bcbRate;
            }
        }

        return currencyEntity.getConversionRate();
    }

    private Double resolveBcbRate(String sourceCurrency) {
        var cache = cacheManager.getCache(BCB_CACHE_NAME);
        if (cache == null) {
            return null;
        }

        var cacheInfo = cache.get(sourceCurrency);
        if (cacheInfo != null) {
            return (Double) cacheInfo.get();
        }

        BCBResponse response = bcbClient.getCotacaoBcb(sourceCurrency, BCB_QUOTE_DATE);
        if (response == null || response.getValue() == null || response.getValue().isEmpty()) {
            return null;
        }

        Double bcbRate = response.getValue().get(0).getCotacaoVenda();
        cache.put(sourceCurrency, bcbRate);
        return bcbRate;
    }

    public record ConversionResult(
            String sourceCurrency,
            String targetCurrency,
            double conversionRate,
            String environment) {
    }
}
