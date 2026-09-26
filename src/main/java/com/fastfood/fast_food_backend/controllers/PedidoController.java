package com.fastfood.fast_food_backend.controllers;

import java.net.URI;
import java.math.BigDecimal;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fastfood.fast_food_backend.entities.Pedido;
import com.fastfood.fast_food_backend.entities.DetallePedido;
import com.fastfood.fast_food_backend.dto.ProcesarPedidoRequest;
import com.fastfood.fast_food_backend.services.PedidoService;

@RestController
@RequestMapping("/api/pedidos")
public class PedidoController {

    private final PedidoService pedidoService;

    public PedidoController(PedidoService pedidoService) {
        this.pedidoService = pedidoService;
    }

    @GetMapping
    public List<Pedido> listar() {
        return pedidoService.listar();
    }

    @GetMapping("/{id}")
    public Pedido buscarPorId(@PathVariable Long id) {
        return pedidoService.buscarPorId(id);
    }

    @PostMapping
    public ResponseEntity<Pedido> registrar(@RequestBody Pedido pedido) {
        Pedido pedidoRegistrado = pedidoService.registrar(pedido);
        URI ubicacion = URI.create("/api/pedidos/" + pedidoRegistrado.getId());
        return ResponseEntity.created(ubicacion).body(pedidoRegistrado);
    }

    @PutMapping("/{id}")
    public Pedido actualizar(@PathVariable Long id, @RequestBody Pedido pedido) {
        return pedidoService.actualizar(id, pedido);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        pedidoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/cliente/{clienteId}")
    public List<Pedido> buscarPorCliente(@PathVariable Long clienteId) {
        return pedidoService.buscarPorCliente(clienteId);
    }

    @GetMapping("/estado/{estado}")
    public List<Pedido> buscarPorEstado(@PathVariable String estado) {
        return pedidoService.buscarPorEstado(estado);
    }

    @PostMapping("/procesar")
    public ResponseEntity<Pedido> procesarPedido(@RequestBody ProcesarPedidoRequest solicitud) {
        Pedido pedido = pedidoService.procesarPedido(solicitud);
        URI ubicacion = URI.create("/api/pedidos/" + pedido.getId());
        return ResponseEntity.created(ubicacion).body(pedido);
    }

    @GetMapping("/{id}/detalles")
    public List<DetallePedido> buscarDetalles(@PathVariable Long id) {
        return pedidoService.buscarDetalles(id);
    }

    @GetMapping("/total-por-estado/{estado}")
    public BigDecimal calcularTotalPorEstado(@PathVariable String estado) {
        return pedidoService.calcularTotalPorEstado(estado);
    }
}
