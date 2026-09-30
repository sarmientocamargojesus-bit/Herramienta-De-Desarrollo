package com.fastfood.fast_food_backend.services;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fastfood.fast_food_backend.entities.Producto;
import com.fastfood.fast_food_backend.entities.Categoria;
import com.fastfood.fast_food_backend.exceptions.RecursoNoEncontradoException;
import com.fastfood.fast_food_backend.exceptions.ReglaNegocioException;
import com.fastfood.fast_food_backend.repositories.ProductoRepository;
import com.fastfood.fast_food_backend.repositories.CategoriaRepository;

@Service
public class ProductoService {

    private final ProductoRepository productoRepository;
    private final CategoriaRepository categoriaRepository;
    private final InventarioService inventarioService;

    public ProductoService(ProductoRepository productoRepository, CategoriaRepository categoriaRepository,
            InventarioService inventarioService) {
        this.productoRepository = productoRepository;
        this.categoriaRepository = categoriaRepository;
        this.inventarioService = inventarioService;
    }

    public List<Producto> listar() {
        return productoRepository.findAll();
    }

    public Producto buscarPorId(Long id) {
        return productoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Producto no encontrado: " + id));
    }

    @Transactional
    public Producto registrar(Producto producto) {
        validar(producto);
        asignarCategoria(producto);
        Producto guardado = productoRepository.save(producto);
        inventarioService.crearParaProductoNuevo(guardado);
        return guardado;
    }

    @Transactional
    public Producto actualizar(Long id, Producto datosProducto) {
        Producto producto = buscarPorId(id);
        int stockAnterior = producto.getStock();
        producto.setNombre(datosProducto.getNombre());
        producto.setDescripcion(datosProducto.getDescripcion());
        producto.setPrecio(datosProducto.getPrecio());
        producto.setStock(datosProducto.getStock());
        producto.setCategoria(datosProducto.getCategoria());
        validar(producto);
        asignarCategoria(producto);
        Producto guardado = productoRepository.save(producto);
        inventarioService.registrarCambioDeStock(guardado, stockAnterior);
        return guardado;
    }

    @Transactional
    public void eliminar(Long id) {
        Producto producto = buscarPorId(id);
        inventarioService.eliminarPorProducto(id);
        productoRepository.delete(producto);
    }

    public List<Producto> buscarPorNombre(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre de búsqueda es obligatorio");
        }
        return productoRepository.buscarPorNombre(nombre);
    }

    public List<Producto> buscarConStockDisponible() {
        return productoRepository.buscarConStockDisponible();
    }

    public List<Producto> buscarPorRangoDePrecios(BigDecimal precioMinimo, BigDecimal precioMaximo) {
        if (precioMinimo == null || precioMaximo == null
                || precioMinimo.compareTo(BigDecimal.ZERO) < 0
                || precioMinimo.compareTo(precioMaximo) > 0) {
            throw new IllegalArgumentException("El rango de precios no es válido");
        }
        return productoRepository.buscarPorRangoDePrecios(precioMinimo, precioMaximo);
    }

    public List<Producto> ordenarPorPrecioDescendente() {
        return productoRepository.ordenarPorPrecioDescendente();
    }

    @Transactional
    public Producto descontarStock(Long id, int cantidad) {
        if (cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor que cero");
        }

        Producto producto = buscarPorId(id);
        if (producto.getStock() < cantidad) {
            throw new ReglaNegocioException(
                    "Stock insuficiente para el producto " + producto.getNombre());
        }

        int stockAnterior = producto.getStock();
        producto.setStock(stockAnterior - cantidad);
        Producto guardado = productoRepository.save(producto);
        inventarioService.registrarCambioDeStock(guardado, stockAnterior);
        return guardado;
    }

    private void validar(Producto producto) {
        if (producto.getNombre() == null || producto.getNombre().isBlank()) {
            throw new IllegalArgumentException("El nombre del producto es obligatorio");
        }
        if (producto.getPrecio() == null || producto.getPrecio().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El precio debe ser mayor que cero");
        }
        if (producto.getStock() < 0) {
            throw new IllegalArgumentException("El stock no puede ser negativo");
        }
        if (producto.getCategoria() == null || producto.getCategoria().isBlank()) {
            throw new IllegalArgumentException("La categoría del producto es obligatoria");
        }
    }

    private void asignarCategoria(Producto producto) {
        Categoria categoria = categoriaRepository.findByNombreIgnoreCase(producto.getCategoria().trim())
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Categoría no encontrada: " + producto.getCategoria()));
        if (!Boolean.TRUE.equals(categoria.getActivo())) {
            throw new ReglaNegocioException("La categoría seleccionada está inactiva");
        }
        producto.setCategoria(categoria.getNombre());
        producto.setCategoriaReferencia(categoria);
    }
}
