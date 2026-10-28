package com.fastfood.fast_food_backend.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/** Recibe únicamente los datos de contacto y entrega que pueden editarse en un pedido. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record ActualizarPedidoRequest(
        String telefono,
        String tipoEntrega,
        String direccionEntrega,
        String observaciones) {
}
