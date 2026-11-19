package com.fastfood.fast_food_backend.dto;

/** Estado al que el personal mueve un pedido: EN_PREPARACION, LISTO o ENTREGADO. */
public record CambiarEstadoPedidoRequest(String estado) {
}
