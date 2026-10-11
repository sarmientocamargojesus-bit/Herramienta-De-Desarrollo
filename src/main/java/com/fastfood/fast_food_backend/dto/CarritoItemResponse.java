package com.fastfood.fast_food_backend.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CarritoItemResponse(
        Long productoId, String nombre, BigDecimal precio, int stock,
        int cantidad, LocalDateTime fechaAgregado) {
}
