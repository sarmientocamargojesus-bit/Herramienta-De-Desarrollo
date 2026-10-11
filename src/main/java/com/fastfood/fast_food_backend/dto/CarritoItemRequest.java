package com.fastfood.fast_food_backend.dto;

import java.time.LocalDateTime;

public record CarritoItemRequest(Long productoId, Integer cantidad, LocalDateTime fechaAgregado) {
}
