package com.fastfood.fast_food_backend.controllers;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fastfood.fast_food_backend.dto.CarritoItemRequest;
import com.fastfood.fast_food_backend.dto.CarritoItemResponse;
import com.fastfood.fast_food_backend.services.CarritoService;

@RestController
@RequestMapping("/api/carrito")
public class CarritoController {

    private final CarritoService carrito;

    public CarritoController(CarritoService carrito) { this.carrito = carrito; }

    @GetMapping
    public List<CarritoItemResponse> listar(Authentication autenticacion) {
        return carrito.listar(autenticacion.getName());
    }

    @PostMapping("/importar")
    public List<CarritoItemResponse> importar(Authentication autenticacion,
            @RequestBody List<CarritoItemRequest> seleccionInvitado) {
        return carrito.importar(autenticacion.getName(), seleccionInvitado);
    }

    @PutMapping("/productos/{productoId}")
    public CarritoItemResponse establecerCantidad(Authentication autenticacion,
            @PathVariable Long productoId, @RequestBody CarritoItemRequest solicitud) {
        if (solicitud == null) throw new IllegalArgumentException("La cantidad es obligatoria");
        return carrito.establecerCantidad(autenticacion.getName(), productoId, solicitud.cantidad());
    }

    @DeleteMapping("/productos/{productoId}")
    public ResponseEntity<Void> quitar(Authentication autenticacion, @PathVariable Long productoId) {
        carrito.quitar(autenticacion.getName(), productoId);
        return ResponseEntity.noContent().build();
    }
}
