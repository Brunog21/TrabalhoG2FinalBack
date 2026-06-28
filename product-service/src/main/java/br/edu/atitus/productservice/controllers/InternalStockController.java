package br.edu.atitus.productservice.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import br.edu.atitus.productservice.dtos.StockAdjustmentDTO;
import br.edu.atitus.productservice.services.ProductStockService;

@RestController
@RequestMapping("/internal/stock")
public class InternalStockController {

    private final ProductStockService productStockService;

    public InternalStockController(ProductStockService productStockService) {
        this.productStockService = productStockService;
    }

    @PostMapping("/debit")
    public ResponseEntity<Void> debitStock(@RequestBody StockAdjustmentDTO request) {
        productStockService.debitStock(request.items());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/restore")
    public ResponseEntity<Void> restoreStock(@RequestBody StockAdjustmentDTO request) {
        productStockService.restoreStock(request.items());
        return ResponseEntity.ok().build();
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<String> handleStatusException(ResponseStatusException exception) {
        return ResponseEntity.status(exception.getStatusCode()).body(exception.getReason());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleException(Exception exception) {
        String message = exception.getMessage() != null ? exception.getMessage().replace("\r\n", "") : "Erro inesperado";
        return ResponseEntity.badRequest().body(message);
    }
}
