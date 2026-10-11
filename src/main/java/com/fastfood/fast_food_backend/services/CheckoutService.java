package com.fastfood.fast_food_backend.services;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fastfood.fast_food_backend.dto.CheckoutRequest;
import com.fastfood.fast_food_backend.dto.CheckoutResponse;
import com.fastfood.fast_food_backend.entities.CarritoItem;
import com.fastfood.fast_food_backend.entities.DetallePedido;
import com.fastfood.fast_food_backend.entities.Pago;
import com.fastfood.fast_food_backend.entities.Pedido;
import com.fastfood.fast_food_backend.entities.Producto;
import com.fastfood.fast_food_backend.entities.Usuario;
import com.fastfood.fast_food_backend.exceptions.RecursoNoEncontradoException;
import com.fastfood.fast_food_backend.exceptions.ReglaNegocioException;
import com.fastfood.fast_food_backend.repositories.CarritoItemRepository;
import com.fastfood.fast_food_backend.repositories.DetallePedidoRepository;
import com.fastfood.fast_food_backend.repositories.PagoRepository;
import com.fastfood.fast_food_backend.repositories.PedidoRepository;
import com.fastfood.fast_food_backend.repositories.ProductoRepository;
import com.fastfood.fast_food_backend.repositories.UsuarioRepository;

@Service
public class CheckoutService {

    private final CarritoItemRepository carrito;
    private final UsuarioRepository usuarios;
    private final ProductoRepository productos;
    private final PedidoRepository pedidos;
    private final DetallePedidoRepository detalles;
    private final PagoRepository pagos;
    private final InventarioService inventario;

    public CheckoutService(CarritoItemRepository carrito, UsuarioRepository usuarios,
            ProductoRepository productos, PedidoRepository pedidos, DetallePedidoRepository detalles,
            PagoRepository pagos, InventarioService inventario) {
        this.carrito = carrito;
        this.usuarios = usuarios;
        this.productos = productos;
        this.pedidos = pedidos;
        this.detalles = detalles;
        this.pagos = pagos;
        this.inventario = inventario;
    }

    @Transactional
    public CheckoutResponse confirmar(String correo, CheckoutRequest solicitud) {
        validarSolicitud(solicitud);
        Usuario usuario = usuarios.bloquearPorCorreo(correo)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));
        if (!Boolean.TRUE.equals(usuario.getActivo()) || !"CLIENTE".equalsIgnoreCase(usuario.getRol())) {
            throw new ReglaNegocioException("Solo un cliente activo puede confirmar el pedido");
        }

        List<CarritoItem> seleccion = carrito.findByUsuarioIdOrderByFechaAgregadoAsc(usuario.getId());
        if (seleccion.isEmpty()) throw new ReglaNegocioException("El carrito está vacío");

        // Orden estable de bloqueos para evitar que dos pagos descuenten el mismo stock simultáneamente.
        Map<Long, Producto> bloqueados = new LinkedHashMap<>();
        seleccion.stream().map(item -> item.getProducto().getId()).distinct().sorted().forEach(id ->
                bloqueados.put(id, productos.bloquearPorId(id)
                        .orElseThrow(() -> new RecursoNoEncontradoException("Producto no encontrado: " + id))));

        BigDecimal subtotal = BigDecimal.ZERO;
        for (CarritoItem item : seleccion) {
            Producto producto = bloqueados.get(item.getProducto().getId());
            if (item.getCantidad() <= 0 || producto.getStock() < item.getCantidad()) {
                throw new ReglaNegocioException("Stock insuficiente para " + producto.getNombre());
            }
            subtotal = subtotal.add(producto.getPrecio().multiply(BigDecimal.valueOf(item.getCantidad())));
        }

        LocalDateTime ahora = LocalDateTime.now();
        Pedido pedido = new Pedido();
        pedido.setUsuario(usuario);
        pedido.setTelefono(usuario.getTelefono());
        pedido.setFecha(ahora);
        pedido.setFechaActualizacion(ahora);
        pedido.setEstado("CONFIRMADO");
        pedido.setSubtotal(subtotal);
        pedido.setCostoEntrega(BigDecimal.ZERO);
        pedido.setTotal(subtotal);
        pedido.setTipoEntrega(solicitud.tipoEntrega().trim().toUpperCase());
        pedido.setDireccionEntrega("DOMICILIO".equals(pedido.getTipoEntrega())
                ? direccion(solicitud.direccionEntrega(), usuario.getDireccion()) : null);
        pedido.setObservaciones(solicitud.observaciones());
        pedido = pedidos.save(pedido);

        for (CarritoItem item : seleccion) {
            Producto producto = bloqueados.get(item.getProducto().getId());
            DetallePedido detalle = new DetallePedido();
            detalle.setPedido(pedido);
            detalle.setProducto(producto);
            detalle.setCantidad(item.getCantidad());
            detalle.setPrecioUnitario(producto.getPrecio());
            detalle.setSubtotal(producto.getPrecio().multiply(BigDecimal.valueOf(item.getCantidad())));
            detalles.save(detalle);

            int stockAnterior = producto.getStock();
            producto.setStock(stockAnterior - item.getCantidad());
            productos.save(producto);
            inventario.registrarCambioDeStock(producto, stockAnterior);
        }

        Pago pago = new Pago();
        pago.setPedido(pedido);
        pago.setMetodo(solicitud.metodoPago().trim().toUpperCase());
        pago.setEstado("APROBADO");
        pago.setMonto(subtotal);
        pago.setFecha(ahora);
        pago = pagos.save(pago);
        carrito.deleteByUsuarioId(usuario.getId());

        return new CheckoutResponse(pedido.getId(), pedido.getEstado(), pago.getId(), pago.getEstado(),
                pedido.getTotal(), pedido.getFecha());
    }

    private void validarSolicitud(CheckoutRequest solicitud) {
        if (solicitud == null || solicitud.tipoEntrega() == null || solicitud.metodoPago() == null) {
            throw new IllegalArgumentException("Indica la entrega y el método de pago");
        }
        String entrega = solicitud.tipoEntrega().trim().toUpperCase();
        String metodo = solicitud.metodoPago().trim().toUpperCase();
        if (!"RECOJO".equals(entrega) && !"DOMICILIO".equals(entrega)) {
            throw new IllegalArgumentException("El tipo de entrega debe ser RECOJO o DOMICILIO");
        }
        if (!"TARJETA".equals(metodo) && !"YAPE".equals(metodo)) {
            throw new IllegalArgumentException("El pago simulado solo admite TARJETA o YAPE");
        }
        // Solo se recibe un marcador de prueba; nunca números de tarjeta, CVV ni claves de Yape.
        if (!"DEMO-APROBADO".equals(solicitud.referenciaPrueba())) {
            throw new IllegalArgumentException("Usa la referencia ficticia DEMO-APROBADO para la simulación");
        }
    }

    private String direccion(String solicitada, String guardada) {
        String valor = solicitada != null && !solicitada.isBlank() ? solicitada.trim() : guardada;
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException("Indica una dirección para entrega a domicilio");
        }
        return valor;
    }
}
