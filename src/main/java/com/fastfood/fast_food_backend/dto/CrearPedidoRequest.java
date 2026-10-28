package com.fastfood.fast_food_backend.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/** Recibe el cliente, la entrega y los productos para crear un pedido pendiente. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record CrearPedidoRequest(
        UsuarioReferencia usuario,
        String telefono,
        String tipoEntrega,
        String direccionEntrega,
        String observaciones,
        List<DetallePedidoRequest> detalles) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record UsuarioReferencia(Long id) {
    }
}
