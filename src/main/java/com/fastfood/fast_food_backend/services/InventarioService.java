package com.fastfood.fast_food_backend.services;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.fastfood.fast_food_backend.entities.Inventario;
import com.fastfood.fast_food_backend.entities.Producto;
import com.fastfood.fast_food_backend.exceptions.RecursoNoEncontradoException;
import com.fastfood.fast_food_backend.exceptions.ReglaNegocioException;
import com.fastfood.fast_food_backend.repositories.InventarioRepository;
import com.fastfood.fast_food_backend.repositories.ProductoRepository;

@Service
public class InventarioService {

    private final InventarioRepository inventarioRepository;
    private final ProductoRepository productoRepository;

    public InventarioService(InventarioRepository inventarioRepository, ProductoRepository productoRepository) {
        this.inventarioRepository = inventarioRepository;
        this.productoRepository = productoRepository;
    }

    public List<Inventario> listar() {
        return inventarioRepository.findAll();
    }

    public List<Inventario> listarStockBajo() {
        return inventarioRepository.buscarProductosConStockBajo();
    }

    public Inventario buscarPorId(Long id) {
        return inventarioRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Inventario no encontrado: " + id));
    }

    public Inventario buscarPorProducto(Long productoId) {
        return inventarioRepository.findByProductoId(productoId)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Inventario no encontrado para el producto: " + productoId));
    }

    public Inventario registrar(Inventario inventario) {
        validar(inventario);
        Long productoId = inventario.getProducto().getId();
        if (inventarioRepository.existsByProductoId(productoId)) {
            throw new ReglaNegocioException("El producto ya tiene una configuración de inventario");
        }
        inventario.setProducto(buscarProducto(productoId));
        inventario.setFechaActualizacion(LocalDateTime.now());
        return inventarioRepository.save(inventario);
    }

    public Inventario actualizar(Long id, Inventario datos) {
        Inventario inventario = buscarPorId(id);
        if (datos.getStockMinimo() < 0) {
            throw new IllegalArgumentException("El stock mínimo no puede ser negativo");
        }
        inventario.setStockMinimo(datos.getStockMinimo());
        inventario.setFechaActualizacion(LocalDateTime.now());
        return inventarioRepository.save(inventario);
    }

    public void eliminar(Long id) {
        inventarioRepository.delete(buscarPorId(id));
    }

    private void validar(Inventario inventario) {
        if (inventario == null || inventario.getProducto() == null || inventario.getProducto().getId() == null) {
            throw new IllegalArgumentException("El producto del inventario es obligatorio");
        }
        if (inventario.getStockMinimo() < 0) {
            throw new IllegalArgumentException("El stock mínimo no puede ser negativo");
        }
    }

    private Producto buscarProducto(Long id) {
        return productoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Producto no encontrado: " + id));
    }
}
