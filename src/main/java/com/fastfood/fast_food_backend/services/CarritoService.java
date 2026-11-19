package com.fastfood.fast_food_backend.services;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fastfood.fast_food_backend.dto.CarritoItemRequest;
import com.fastfood.fast_food_backend.dto.CarritoItemResponse;
import com.fastfood.fast_food_backend.entities.CarritoItem;
import com.fastfood.fast_food_backend.entities.Producto;
import com.fastfood.fast_food_backend.entities.Usuario;
import com.fastfood.fast_food_backend.exceptions.RecursoNoEncontradoException;
import com.fastfood.fast_food_backend.exceptions.ReglaNegocioException;
import com.fastfood.fast_food_backend.repositories.CarritoItemRepository;
import com.fastfood.fast_food_backend.repositories.ProductoRepository;
import com.fastfood.fast_food_backend.repositories.UsuarioRepository;

@Service
public class CarritoService {

    private static final int CANTIDAD_MAXIMA = 100;
    private final CarritoItemRepository items;
    private final ProductoRepository productos;
    private final UsuarioRepository usuarios;

    public CarritoService(CarritoItemRepository items, ProductoRepository productos, UsuarioRepository usuarios) {
        this.items = items;
        this.productos = productos;
        this.usuarios = usuarios;
    }

    @Transactional(readOnly = true)
    public List<CarritoItemResponse> listar(String correo) {
        Usuario usuario = obtenerCliente(correo, false);
        return items.findByUsuarioIdOrderByFechaAgregadoAsc(usuario.getId()).stream()
                .map(this::respuesta).toList();
    }

    @Transactional
    public List<CarritoItemResponse> importar(String correo, List<CarritoItemRequest> seleccionInvitado) {
        if (seleccionInvitado == null || seleccionInvitado.isEmpty() || seleccionInvitado.size() > 100) {
            throw new IllegalArgumentException("Envía entre 1 y 100 productos del carrito de invitado");
        }
        Usuario usuario = obtenerCliente(correo, true);
        for (CarritoItemRequest solicitud : seleccionInvitado) {
            validar(solicitud);
            // Un producto que ya no se vende se omite para no perder el resto del carrito del invitado.
            Producto producto = productos.findById(solicitud.productoId())
                    .filter(Producto::categoriaDisponible).orElse(null);
            if (producto == null) continue;
            CarritoItem item = items.findByUsuarioIdAndProductoId(usuario.getId(), producto.getId())
                    .orElseGet(() -> nuevoItem(usuario, producto, fechaValida(solicitud.fechaAgregado())));
            // El máximo hace que reintentar la importación no duplique unidades.
            item.setCantidad(Math.max(item.getCantidad(), solicitud.cantidad()));
            LocalDateTime fecha = fechaValida(solicitud.fechaAgregado());
            if (fecha.isBefore(item.getFechaAgregado())) item.setFechaAgregado(fecha);
            items.save(item);
        }
        return items.findByUsuarioIdOrderByFechaAgregadoAsc(usuario.getId()).stream()
                .map(this::respuesta).toList();
    }

    @Transactional
    public CarritoItemResponse establecerCantidad(String correo, Long productoId, Integer cantidad) {
        if (cantidad == null || cantidad < 1 || cantidad > CANTIDAD_MAXIMA) {
            throw new IllegalArgumentException("La cantidad debe estar entre 1 y " + CANTIDAD_MAXIMA);
        }
        Usuario usuario = obtenerCliente(correo, true);
        Producto producto = buscarProducto(productoId);
        if (producto.getStock() < cantidad) {
            throw new ReglaNegocioException("Stock insuficiente para " + producto.getNombre());
        }
        CarritoItem item = items.findByUsuarioIdAndProductoId(usuario.getId(), productoId)
                .orElseGet(() -> nuevoItem(usuario, producto, LocalDateTime.now()));
        item.setCantidad(cantidad);
        return respuesta(items.save(item));
    }

    @Transactional
    public void quitar(String correo, Long productoId) {
        Usuario usuario = obtenerCliente(correo, true);
        items.findByUsuarioIdAndProductoId(usuario.getId(), productoId).ifPresent(items::delete);
    }

    private Usuario obtenerCliente(String correo, boolean bloquear) {
        if (correo == null || correo.isBlank()) throw new IllegalArgumentException("Inicia sesión como cliente");
        Usuario usuario = (bloquear ? usuarios.bloquearPorCorreo(correo) : usuarios.findByCorreoIgnoreCase(correo))
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));
        if (!Boolean.TRUE.equals(usuario.getActivo()) || !"CLIENTE".equalsIgnoreCase(usuario.getRol())) {
            throw new ReglaNegocioException("Solo un cliente activo puede usar el carrito");
        }
        return usuario;
    }

    private Producto buscarProducto(Long productoId) {
        if (productoId == null) throw new IllegalArgumentException("El producto es obligatorio");
        Producto producto = productos.findById(productoId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Producto no encontrado: " + productoId));
        if (!producto.categoriaDisponible()) {
            throw new ReglaNegocioException("El producto no está disponible: " + producto.getNombre());
        }
        return producto;
    }

    private void validar(CarritoItemRequest solicitud) {
        if (solicitud == null || solicitud.productoId() == null || solicitud.cantidad() == null
                || solicitud.cantidad() < 1 || solicitud.cantidad() > CANTIDAD_MAXIMA) {
            throw new IllegalArgumentException("Cada producto debe tener identificador y cantidad entre 1 y 100");
        }
    }

    private LocalDateTime fechaValida(LocalDateTime fecha) {
        if (fecha == null) return LocalDateTime.now();
        if (fecha.isAfter(LocalDateTime.now().plusMinutes(5))) {
            throw new IllegalArgumentException("La fecha de agregado no puede estar en el futuro");
        }
        return fecha;
    }

    private CarritoItem nuevoItem(Usuario usuario, Producto producto, LocalDateTime fecha) {
        CarritoItem item = new CarritoItem();
        item.setUsuario(usuario);
        item.setProducto(producto);
        item.setFechaAgregado(fecha);
        return item;
    }

    private CarritoItemResponse respuesta(CarritoItem item) {
        Producto producto = item.getProducto();
        return new CarritoItemResponse(producto.getId(), producto.getNombre(), producto.getPrecio(),
                producto.getStock(), item.getCantidad(), item.getFechaAgregado());
    }
}
