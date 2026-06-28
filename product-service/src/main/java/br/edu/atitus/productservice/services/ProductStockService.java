package br.edu.atitus.productservice.services;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import br.edu.atitus.productservice.dtos.StockItemDTO;
import br.edu.atitus.productservice.entities.ProductEntity;
import br.edu.atitus.productservice.repositories.ProductRepository;

@Service
public class ProductStockService {

    private final ProductRepository repository;

    public ProductStockService(ProductRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public void debitStock(List<StockItemDTO> items) {
        Map<Long, Integer> quantities = aggregateQuantities(items);
        Map<Long, ProductEntity> products = loadAndValidateForDebit(quantities);
        applyStockChanges(products, quantities, -1);
    }

    @Transactional
    public void restoreStock(List<StockItemDTO> items) {
        Map<Long, Integer> quantities = aggregateQuantities(items);
        Map<Long, ProductEntity> products = loadProducts(quantities);
        applyStockChanges(products, quantities, 1);
    }

    private Map<Long, Integer> aggregateQuantities(List<StockItemDTO> items) {
        if (items == null || items.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nenhum item informado");
        }

        Map<Long, Integer> quantities = new HashMap<>();
        for (StockItemDTO item : items) {
            if (item.productId() == null || item.productId() <= 0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Produto inválido");
            }
            if (item.quantity() == null || item.quantity() <= 0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Quantidade inválida");
            }
            quantities.merge(item.productId(), item.quantity(), Integer::sum);
        }
        return quantities;
    }

    private Map<Long, ProductEntity> loadAndValidateForDebit(Map<Long, Integer> quantities) {
        Map<Long, ProductEntity> products = loadProducts(quantities);

        for (Map.Entry<Long, Integer> entry : quantities.entrySet()) {
            ProductEntity product = products.get(entry.getKey());
            int currentStock = product.getStock() != null ? product.getStock() : 0;
            if (currentStock < entry.getValue()) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Estoque insuficiente para o produto: " + product.getDescription());
            }
        }

        return products;
    }

    private Map<Long, ProductEntity> loadProducts(Map<Long, Integer> quantities) {
        Map<Long, ProductEntity> products = new HashMap<>();
        for (Long productId : quantities.keySet()) {
            ProductEntity product = repository.findById(productId)
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND,
                            "Produto não encontrado: " + productId));
            products.put(productId, product);
        }
        return products;
    }

    private void applyStockChanges(
            Map<Long, ProductEntity> products,
            Map<Long, Integer> quantities,
            int direction) {
        for (Map.Entry<Long, Integer> entry : quantities.entrySet()) {
            ProductEntity product = products.get(entry.getKey());
            int currentStock = product.getStock() != null ? product.getStock() : 0;
            product.setStock(currentStock + (entry.getValue() * direction));
            repository.save(product);
        }
    }
}
