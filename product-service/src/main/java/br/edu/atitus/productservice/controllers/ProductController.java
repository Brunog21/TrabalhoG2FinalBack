package br.edu.atitus.productservice.controllers;

import br.edu.atitus.productservice.clients.CurrencyClient;
import br.edu.atitus.productservice.clients.CurrencyResponse;
import br.edu.atitus.productservice.dtos.ProductDTO;
import br.edu.atitus.productservice.entities.ProductEntity;
import br.edu.atitus.productservice.repositories.ProductRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("products")
public class ProductController {

    private final ProductRepository repository;
    private final CurrencyClient currencyClient;
    private final CacheManager cacheManager;

    public ProductController(ProductRepository repository, CurrencyClient currencyClient, CacheManager cacheManager) {
        this.repository = repository;
        this.currencyClient = currencyClient;
        this.cacheManager = cacheManager;
    }

    @Value("${server.port}")
    private String port;

    @GetMapping("/{id}")
    public ResponseEntity<ProductDTO> getProduct(
            @PathVariable Long id,
            @RequestParam String targetCurrency) throws Exception {
        targetCurrency = targetCurrency.toUpperCase();

        ProductEntity entity = repository.findById(id)
                .orElseThrow(() -> new Exception("Product not found!"));
        Double convertedPrice;
        String environment = "Product-service running on port: " + port;
        String requestCurrency = targetCurrency;

        if (targetCurrency.equals(entity.getCurrency())) {
            convertedPrice = entity.getPrice();
        } else {
            String nameCache = "ConvertedValue";
            String keyCache = entity.getCurrency() + "-" + targetCurrency;
            Double convertedValue = cacheManager.getCache(nameCache).get(keyCache, Double.class);
            // convertedValue=null; //Para testar sem cache
            if (convertedValue == null) {
                CurrencyResponse currency = currencyClient.getCurrency(entity.getCurrency(), targetCurrency);
                if (currency != null) {
                    convertedPrice = currency.conversionRate() * entity.getPrice();
                    environment = environment + " - " + currency.environment();
                    cacheManager.getCache(nameCache).put(keyCache, currency.conversionRate());
                } else {
                    convertedPrice = -1.0;
                    environment = environment + " - Currency Fallback";
                }
            } else {
                convertedPrice = convertedValue * entity.getPrice();
                environment = environment + " - Currency in cache";
            }
        }

        ProductDTO dto = new ProductDTO(
                entity.getId(),
                entity.getDescription(),
                entity.getBrand(),
                entity.getModel(),
                entity.getCurrency(),
                entity.getPrice(),
                entity.getStock(),
                entity.getImageURL(),
                convertedPrice,
                environment,
                requestCurrency);

        return ResponseEntity.ok(dto);
    }

    @GetMapping("/noconverter/{idProduct}")
    public ResponseEntity<ProductDTO> getProductNoConverter(@PathVariable Long idProduct) throws Exception {
        var product = repository.findById(idProduct)
                .orElseThrow(() -> new Exception("Produto não encontrado!"));

        ProductDTO dto = new ProductDTO(
                product.getId(),
                product.getDescription(),
                product.getBrand(),
                product.getModel(),
                product.getCurrency(),
                product.getPrice(),
                product.getStock(),
                product.getImageURL(),
                -1.,
                "Product-service running on port: " + port,
                null);

        return ResponseEntity.ok(dto);
    }

    @GetMapping
    public ResponseEntity<Page<ProductDTO>> getAllProducts(
            @RequestParam String targetCurrency,
            @PageableDefault(page = 0, size = 5, sort = "description", direction = Sort.Direction.ASC) Pageable pageable)
            throws Exception {

        final String finalTargetCurrency = targetCurrency.toUpperCase();

        Page<ProductEntity> products = repository.findAll(pageable);

        Page<ProductDTO> productDTOs = products.map(product -> {

            String environment = "Product-service running on port: " + port;
            Double convertedPrice = null;

            if (finalTargetCurrency.equals(product.getCurrency())) {

                convertedPrice = product.getPrice();

            } else {

                String nameCache = "ConvertedValue";
                String keyCache = product.getCurrency() + "-" + finalTargetCurrency;

                Double convertedValue = cacheManager.getCache(nameCache).get(keyCache, Double.class);

                if (convertedValue == null) {

                    CurrencyResponse currency = currencyClient.getCurrency(
                            product.getCurrency(),
                            finalTargetCurrency);

                    if (currency != null) {

                        convertedPrice = currency.conversionRate() *
                                product.getPrice();

                        environment = environment +
                                " - " +
                                currency.environment();

                        cacheManager
                                .getCache(nameCache)
                                .put(keyCache, currency.conversionRate());

                    } else {

                        convertedPrice = -1.0;
                        environment = environment +
                                " - Currency Fallback";
                    }

                } else {

                    convertedPrice = convertedValue *
                            product.getPrice();

                    environment = environment +
                            " - Currency in cache";
                }
            }

            return new ProductDTO(
                    product.getId(),
                    product.getDescription(),
                    product.getBrand(),
                    product.getModel(),
                    product.getCurrency(),
                    product.getPrice(),
                    product.getStock(),
                    product.getImageURL(),
                    convertedPrice,
                    environment,
                    targetCurrency);
        });

        return ResponseEntity.ok(productDTOs);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleException(Exception e) {
        String message = e.getMessage().replace("/r/n", "");
        return ResponseEntity.badRequest().body(message);
    }

}
