package com.fastfood.fast_food_backend.controllers;

import java.net.URI;
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

import com.fastfood.fast_food_backend.entities.Inventario;
import com.fastfood.fast_food_backend.services.InventarioService;

@RestController
@RequestMapping("/api/inventarios")
public class InventarioController {

    private final InventarioService inventarioService;

    public InventarioController(InventarioService inventarioService) {
        this.inventarioService = inventarioService;
    }

    @GetMapping
    public List<Inventario> listar() {
        return inventarioService.listar();
    }

    @GetMapping("/bajo-minimo")
    public List<Inventario> listarStockBajo() {
        return inventarioService.listarStockBajo();
    }

    @GetMapping("/{id}")
    public Inventario buscarPorId(@PathVariable Long id) {
        return inventarioService.buscarPorId(id);
    }

    @GetMapping("/producto/{productoId}")
    public Inventario buscarPorProducto(@PathVariable Long productoId) {
        return inventarioService.buscarPorProducto(productoId);
    }

    @PostMapping
    public ResponseEntity<Inventario> registrar(@RequestBody Inventario inventario) {
        Inventario registrado = inventarioService.registrar(inventario);
        return ResponseEntity.created(URI.create("/api/inventarios/" + registrado.getId())).body(registrado);
    }

    @PutMapping("/{id}")
    public Inventario actualizar(@PathVariable Long id, @RequestBody Inventario inventario) {
        return inventarioService.actualizar(id, inventario);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        inventarioService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
