package com.fastfood.fast_food_backend.dto;

public record CheckoutRequest(
        String tipoEntrega, String direccionEntrega, String observaciones,
        String metodoPago, String referenciaPrueba) {
}
