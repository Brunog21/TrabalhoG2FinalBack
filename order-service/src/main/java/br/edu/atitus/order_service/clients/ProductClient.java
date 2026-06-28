package br.edu.atitus.order_service.clients;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "product-service")
public interface ProductClient {

    @GetMapping("/products/noconverter/{id}")
    ProductResponse getProductById(@PathVariable("id") Long id);
    
    @GetMapping("/products/{id}?targetCurrency={targetCurrency}")
    ProductResponse getProductByIdWithCurrency(@PathVariable Long id, @PathVariable String targetCurrency);

    @PostMapping("/internal/stock/debit")
    void debitStock(@RequestBody StockAdjustmentRequest request);

    @PostMapping("/internal/stock/restore")
    void restoreStock(@RequestBody StockAdjustmentRequest request);
}