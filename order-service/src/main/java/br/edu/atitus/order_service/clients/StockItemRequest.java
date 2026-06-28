package br.edu.atitus.order_service.clients;

public record StockItemRequest(Long productId, Integer quantity) {}
