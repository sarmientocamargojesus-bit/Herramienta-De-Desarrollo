package com.fastfood.fast_food_backend.controllers;

import java.net.URI;
import java.math.BigDecimal;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
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
    @PreAuthorize("hasAnyRole('EMPLEADO','ADMINISTRADOR')")
    public List<Pedido> listar() {
        return pedidoService.listar();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('EMPLEADO','ADMINISTRADOR') or @autorizacionPedido.esPropietario(#id, authentication)")
    public Pedido buscarPorId(@PathVariable Long id) {
        return pedidoService.buscarPorId(id);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('EMPLEADO','ADMINISTRADOR') or @autorizacionPedido.puedeCrearPedido(#pedido, authentication)")
    public ResponseEntity<Pedido> registrar(@RequestBody Pedido pedido) {
        Pedido pedidoRegistrado = pedidoService.registrar(pedido);
        URI ubicacion = URI.create("/api/pedidos/" + pedidoRegistrado.getId());
        return ResponseEntity.created(ubicacion).body(pedidoRegistrado);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('EMPLEADO','ADMINISTRADOR')")
    public Pedido actualizar(@PathVariable Long id, @RequestBody Pedido pedido) {
        return pedidoService.actualizar(id, pedido);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('EMPLEADO','ADMINISTRADOR')")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        pedidoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/usuario/{usuarioId}")
    @PreAuthorize("hasAnyRole('EMPLEADO','ADMINISTRADOR') or @autorizacionPedido.esMismoUsuario(#usuarioId, authentication)")
    public List<Pedido> buscarPorUsuario(@PathVariable Long usuarioId) {
        return pedidoService.buscarPorUsuario(usuarioId);
    }

    @GetMapping("/estado/{estado}")
    @PreAuthorize("hasAnyRole('EMPLEADO','ADMINISTRADOR')")
    public List<Pedido> buscarPorEstado(@PathVariable String estado) {
        return pedidoService.buscarPorEstado(estado);
    }

    @PostMapping("/procesar")
    @PreAuthorize("hasAnyRole('EMPLEADO','ADMINISTRADOR') or @autorizacionPedido.puedeProcesarPedido(#solicitud, authentication)")
    public ResponseEntity<Pedido> procesarPedido(@RequestBody ProcesarPedidoRequest solicitud) {
        Pedido pedido = pedidoService.procesarPedido(solicitud);
        URI ubicacion = URI.create("/api/pedidos/" + pedido.getId());
        return ResponseEntity.created(ubicacion).body(pedido);
    }

    @GetMapping("/{id}/detalles")
    @PreAuthorize("hasAnyRole('EMPLEADO','ADMINISTRADOR') or @autorizacionPedido.esPropietario(#id, authentication)")
    public List<DetallePedido> buscarDetalles(@PathVariable Long id) {
        return pedidoService.buscarDetalles(id);
    }

    @GetMapping("/total-por-estado/{estado}")
    @PreAuthorize("hasAnyRole('EMPLEADO','ADMINISTRADOR')")
    public BigDecimal calcularTotalPorEstado(@PathVariable String estado) {
        return pedidoService.calcularTotalPorEstado(estado);
    }

    @GetMapping("/mios")
    @PreAuthorize("hasAnyRole('CLIENTE','ADMINISTRADOR')")
    public List<Pedido> listarMisPedidos(Authentication authentication) {
        return pedidoService.buscarPorCorreoUsuario(authentication.getName());
    }
}
