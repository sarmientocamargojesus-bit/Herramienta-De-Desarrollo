package com.fastfood.fast_food_backend.controllers;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fastfood.fast_food_backend.entities.Pago;
import com.fastfood.fast_food_backend.services.PagoService;

@RestController
@RequestMapping("/api/pagos")
public class PagoController {

    private final PagoService pagoService;

    public PagoController(PagoService pagoService) {
        this.pagoService = pagoService;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public List<Pago> listar() {
        return pagoService.listar();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR') or @autorizacionPedido.esPropietarioPago(#id, authentication)")
    public Pago buscarPorId(@PathVariable Long id) {
        return pagoService.buscarPorId(id);
    }

    @GetMapping("/pedido/{pedidoId}")
    @PreAuthorize("hasRole('ADMINISTRADOR') or @autorizacionPedido.esPropietario(#pedidoId, authentication)")
    public Pago buscarPorPedido(@PathVariable Long pedidoId) {
        return pagoService.buscarPorPedido(pedidoId);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRADOR') or @autorizacionPedido.puedeRegistrarPago(#pago, authentication)")
    public ResponseEntity<Pago> registrar(@RequestBody Pago pago) {
        Pago registrado = pagoService.registrar(pago);
        return ResponseEntity.created(URI.create("/api/pagos/" + registrado.getId())).body(registrado);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public Pago actualizar(@PathVariable Long id, @RequestBody Pago pago) {
        return pagoService.actualizar(id, pago);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        pagoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
