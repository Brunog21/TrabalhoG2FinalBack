package br.edu.atitus.productservice.dtos;

import java.util.List;

public record StockAdjustmentDTO(List<StockItemDTO> items) {}
