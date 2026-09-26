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
import org.springframework.web.bind.annotation.RequestParam;

import com.fastfood.fast_food_backend.entities.Producto;
import com.fastfood.fast_food_backend.services.ProductoService;

@RestController
@RequestMapping("/api/productos")
public class ProductoController {

    private final ProductoService productoService;

    public ProductoController(ProductoService productoService) {
        this.productoService = productoService;
    }

    @GetMapping
    public List<Producto> listar() {
        return productoService.listar();
    }

    @GetMapping("/{id}")
    public Producto buscarPorId(@PathVariable Long id) {
        return productoService.buscarPorId(id);
    }

    @PostMapping
    public ResponseEntity<Producto> registrar(@RequestBody Producto producto) {
        Producto productoRegistrado = productoService.registrar(producto);
        URI ubicacion = URI.create("/api/productos/" + productoRegistrado.getId());
        return ResponseEntity.created(ubicacion).body(productoRegistrado);
    }

    @PutMapping("/{id}")
    public Producto actualizar(@PathVariable Long id, @RequestBody Producto producto) {
        return productoService.actualizar(id, producto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        productoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/buscar")
    public List<Producto> buscarPorNombre(@RequestParam String nombre) {
        return productoService.buscarPorNombre(nombre);
    }

    @GetMapping("/disponibles")
    public List<Producto> buscarConStockDisponible() {
        return productoService.buscarConStockDisponible();
    }

    @GetMapping("/rango-precios")
    public List<Producto> buscarPorRangoDePrecios(
            @RequestParam BigDecimal minimo,
            @RequestParam BigDecimal maximo) {
        return productoService.buscarPorRangoDePrecios(minimo, maximo);
    }

    @GetMapping("/ordenados-precio")
    public List<Producto> ordenarPorPrecioDescendente() {
        return productoService.ordenarPorPrecioDescendente();
    }

    @PutMapping("/{id}/descontar-stock")
    public Producto descontarStock(@PathVariable Long id, @RequestParam int cantidad) {
        return productoService.descontarStock(id, cantidad);
    }
}
