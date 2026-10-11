package com.fastfood.fast_food_backend.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CheckoutResponse(
        Long pedidoId, String estadoPedido, Long pagoId, String estadoPago,
        BigDecimal total, LocalDateTime fechaPedido) {
}
