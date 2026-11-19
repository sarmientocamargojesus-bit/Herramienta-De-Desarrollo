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
        return inventarioRepository.listarConProducto();
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
        // Registrar siempre crea una configuración nueva; el id y las fechas de alerta los define el servidor.
        inventario.setId(null);
        inventario.setFechaStockBajo(null);
        inventario.setFechaAgotado(null);
        inventario.setProducto(buscarProducto(productoId));
        LocalDateTime ahora = LocalDateTime.now();
        inventario.setFechaActualizacion(ahora);
        if (inventario.getProducto().getStock() <= inventario.getStockMinimo()) {
            inventario.setFechaStockBajo(ahora);
        }
        if (inventario.getProducto().getStock() == 0) {
            inventario.setFechaAgotado(ahora);
        }
        return inventarioRepository.save(inventario);
    }

    public Inventario crearParaProductoNuevo(Producto producto) {
        Inventario inventario = new Inventario();
        inventario.setProducto(producto);
        inventario.setStockMinimo(0);
        LocalDateTime ahora = LocalDateTime.now();
        inventario.setFechaActualizacion(ahora);
        if (producto.getStock() == 0) {
            inventario.setFechaStockBajo(ahora);
            inventario.setFechaAgotado(ahora);
        }
        return inventarioRepository.save(inventario);
    }

    public void registrarCambioDeStock(Producto producto, int stockAnterior) {
        if (producto.getStock() == stockAnterior) {
            return;
        }
        inventarioRepository.findByProductoId(producto.getId()).ifPresent(inventario -> {
            LocalDateTime ahora = LocalDateTime.now();
            int stockActual = producto.getStock();
            int minimo = inventario.getStockMinimo();
            boolean cambio = false;

            if (stockActual <= minimo && (stockAnterior > minimo || inventario.getFechaStockBajo() == null)) {
                inventario.setFechaStockBajo(ahora);
                cambio = true;
            } else if (stockActual > minimo && inventario.getFechaStockBajo() != null) {
                inventario.setFechaStockBajo(null);
                cambio = true;
            }

            if (stockActual == 0 && (stockAnterior > 0 || inventario.getFechaAgotado() == null)) {
                inventario.setFechaAgotado(ahora);
                cambio = true;
            } else if (stockActual > 0 && inventario.getFechaAgotado() != null) {
                inventario.setFechaAgotado(null);
                cambio = true;
            }

            if (cambio) {
                inventarioRepository.save(inventario);
            }
        });
    }

    public Inventario actualizar(Long id, Inventario datos) {
        Inventario inventario = buscarPorId(id);
        if (datos.getStockMinimo() < 0) {
            throw new IllegalArgumentException("El stock mínimo no puede ser negativo");
        }
        int minimoAnterior = inventario.getStockMinimo();
        int stock = inventario.getProducto().getStock();
        LocalDateTime ahora = LocalDateTime.now();
        inventario.setStockMinimo(datos.getStockMinimo());
        inventario.setFechaActualizacion(ahora);
        if (stock <= datos.getStockMinimo()
                && (stock > minimoAnterior || inventario.getFechaStockBajo() == null)) {
            inventario.setFechaStockBajo(ahora);
        } else if (stock > datos.getStockMinimo()) {
            inventario.setFechaStockBajo(null);
        }
        return inventarioRepository.save(inventario);
    }

    public void eliminar(Long id) {
        inventarioRepository.delete(buscarPorId(id));
    }

    public void eliminarPorProducto(Long productoId) {
        inventarioRepository.findByProductoId(productoId).ifPresent(inventarioRepository::delete);
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
