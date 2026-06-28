package br.edu.atitus.order_service.services;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import br.edu.atitus.order_service.clients.CurrencyClient;
import br.edu.atitus.order_service.clients.CurrencyResponse;
import br.edu.atitus.order_service.clients.ProductClient;
import br.edu.atitus.order_service.clients.ProductResponse;
import br.edu.atitus.order_service.clients.StockAdjustmentRequest;
import br.edu.atitus.order_service.clients.StockItemRequest;
import br.edu.atitus.order_service.entities.OrderEntity;
import br.edu.atitus.order_service.entities.OrderItemEntity;
import br.edu.atitus.order_service.repositories.OrderRepository;

@Service
@Transactional
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductClient productClient;
    private final CurrencyClient currencyClient;

    public OrderService(OrderRepository orderRepository, ProductClient productClient, CurrencyClient currencyClient) {
        this.orderRepository = orderRepository;
        this.productClient = productClient;
		this.currencyClient = currencyClient;
    }

    public OrderEntity createOrder(OrderEntity order, Long userId) {
        StockAdjustmentRequest stockAdjustment = toStockAdjustment(order);
        productClient.debitStock(stockAdjustment);

        try {
            order.setCustomerId(userId);
            return orderRepository.save(order);
        } catch (RuntimeException exception) {
            productClient.restoreStock(stockAdjustment);
            throw exception;
        }
    }

    private StockAdjustmentRequest toStockAdjustment(OrderEntity order) {
        if (order.getItems() == null || order.getItems().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Pedido sem itens");
        }

        List<StockItemRequest> items = order.getItems().stream()
                .map(item -> new StockItemRequest(item.getProductId(), item.getQuantity()))
                .collect(Collectors.toList());

        return new StockAdjustmentRequest(items);
    }

    public Page<OrderEntity> findOrdersByCustomerId(Long customerId, String targetCurrency, Pageable pageable) {
    	Page<OrderEntity> orders = orderRepository.findByCustomerId(customerId, pageable);
    	orders.forEach(order -> enrichOrder(order, targetCurrency));
        return orders;
    }

    public Optional<OrderEntity> findOrderById(Long orderId, Long customerId, String targetCurrency) {
        return orderRepository.findByIdAndCustomerId(orderId, customerId)
                .map(order -> {
                    enrichOrder(order, targetCurrency);
                    return order;
                });
    }

    public Page<OrderEntity> findAllOrders(String targetCurrency, Pageable pageable) {
        Page<OrderEntity> orders = orderRepository.findAllWithItems(pageable);
        orders.forEach(order -> enrichOrder(order, targetCurrency));
        return orders;
    }

    public void requireAdmin(Integer userType) {
        if (userType == null || userType != 0) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Usuário sem permissão");
        }
    }

    private void enrichOrder(OrderEntity order, String targetCurrency) {
        double totalPrice = 0.0;
        double totalConvertedPrice = 0.0;
        String normalizedTargetCurrency = normalizeCurrency(targetCurrency);

        if (order.getItems() == null) {
            order.setTotalPrice(totalPrice);
            order.setTotalConvertedPrice(totalConvertedPrice);
            return;
        }

        for (OrderItemEntity item : order.getItems()) {
            item.setProduct(resolveProductSnapshot(item));
            totalPrice += item.getPriceAtPurchase() * item.getQuantity();

            double conversionRate = resolveConversionRate(
                    normalizeCurrency(item.getCurrencyAtPurchase()),
                    normalizedTargetCurrency);
            item.setConvertedPriceAtPruchase(item.getPriceAtPurchase() * conversionRate);
            totalConvertedPrice += item.getConvertedPriceAtPruchase() * item.getQuantity();
        }

        order.setTotalPrice(totalPrice);
        order.setTotalConvertedPrice(totalConvertedPrice);
    }

    private ProductResponse resolveProductSnapshot(OrderItemEntity item) {
        try {
            return productClient.getProductById(item.getProductId());
        } catch (RuntimeException exception) {
            String currency = normalizeCurrency(item.getCurrencyAtPurchase());
            return new ProductResponse(
                    item.getProductId(),
                    "Produto #" + item.getProductId(),
                    "",
                    "",
                    item.getPriceAtPurchase(),
                    currency,
                    0,
                    null,
                    "product-unavailable",
                    item.getPriceAtPurchase());
        }
    }

    private String normalizeCurrency(String currency) {
        if (currency == null || currency.isBlank()) {
            return "BRL";
        }
        return currency.trim().toUpperCase();
    }

    private double resolveConversionRate(String sourceCurrency, String targetCurrency) {
        if (sourceCurrency.equalsIgnoreCase(targetCurrency)) {
            return 1.0;
        }

        try {
            CurrencyResponse currencyResponse = currencyClient.getCurrency(sourceCurrency, targetCurrency);
            if (currencyResponse != null && currencyResponse.getConversionRate() > 0) {
                return currencyResponse.getConversionRate();
            }
        } catch (RuntimeException exception) {
            // Mantém o pedido listável mesmo se a conversão falhar.
        }

        return 1.0;
    }
}
