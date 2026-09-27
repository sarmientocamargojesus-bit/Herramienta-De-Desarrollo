package com.fastfood.fast_food_backend.services;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fastfood.fast_food_backend.dto.DetallePedidoRequest;
import com.fastfood.fast_food_backend.dto.ProcesarPedidoRequest;
import com.fastfood.fast_food_backend.entities.DetallePedido;
import com.fastfood.fast_food_backend.entities.Usuario;
import com.fastfood.fast_food_backend.entities.Pedido;
import com.fastfood.fast_food_backend.entities.Producto;
import com.fastfood.fast_food_backend.exceptions.RecursoNoEncontradoException;
import com.fastfood.fast_food_backend.exceptions.ReglaNegocioException;
import com.fastfood.fast_food_backend.repositories.UsuarioRepository;
import com.fastfood.fast_food_backend.repositories.DetallePedidoRepository;
import com.fastfood.fast_food_backend.repositories.PedidoRepository;
import com.fastfood.fast_food_backend.repositories.ProductoRepository;

@Service
public class PedidoService {

    private final PedidoRepository pedidoRepository;
    private final UsuarioRepository usuarioRepository;
    private final ProductoRepository productoRepository;
    private final DetallePedidoRepository detallePedidoRepository;

    public PedidoService(
            PedidoRepository pedidoRepository,
            UsuarioRepository usuarioRepository,
            ProductoRepository productoRepository,
            DetallePedidoRepository detallePedidoRepository) {
        this.pedidoRepository = pedidoRepository;
        this.usuarioRepository = usuarioRepository;
        this.productoRepository = productoRepository;
        this.detallePedidoRepository = detallePedidoRepository;
    }

    public List<Pedido> listar() {
        return pedidoRepository.findAll();
    }

    public Pedido buscarPorId(Long id) {
        return pedidoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Pedido no encontrado: " + id));
    }

    public Pedido registrar(Pedido pedido) {
        Usuario usuario = obtenerClienteActivo(pedido.getUsuario() != null ? pedido.getUsuario().getId() : null);
        pedido.setUsuario(usuario);
        completarDatosEconomicos(pedido);
        if (pedido.getTelefono() == null || pedido.getTelefono().isBlank()) {
            pedido.setTelefono(usuario.getTelefono());
        }
        completarDireccion(pedido, usuario);
        if (pedido.getFecha() == null) {
            pedido.setFecha(LocalDateTime.now());
        }
        pedido.setFechaActualizacion(LocalDateTime.now());
        validar(pedido);
        return pedidoRepository.save(pedido);
    }

    public Pedido actualizar(Long id, Pedido datosPedido) {
        Pedido pedido = buscarPorId(id);
        Usuario usuario = obtenerClienteActivo(
                datosPedido.getUsuario() != null ? datosPedido.getUsuario().getId() : null);
        datosPedido.setUsuario(usuario);
        completarDatosEconomicos(datosPedido);
        if (datosPedido.getTelefono() == null || datosPedido.getTelefono().isBlank()) {
            datosPedido.setTelefono(usuario.getTelefono());
        }
        completarDireccion(datosPedido, usuario);
        validar(datosPedido);

        pedido.setUsuario(usuario);
        pedido.setTelefono(datosPedido.getTelefono());
        pedido.setEstado(datosPedido.getEstado());
        pedido.setSubtotal(datosPedido.getSubtotal());
        pedido.setCostoEntrega(datosPedido.getCostoEntrega());
        pedido.setTotal(datosPedido.getTotal());
        pedido.setTipoEntrega(datosPedido.getTipoEntrega());
        pedido.setDireccionEntrega(datosPedido.getDireccionEntrega());
        pedido.setObservaciones(datosPedido.getObservaciones());
        if (datosPedido.getFecha() != null) {
            pedido.setFecha(datosPedido.getFecha());
        }
        pedido.setFechaActualizacion(LocalDateTime.now());
        return pedidoRepository.save(pedido);
    }

    public void eliminar(Long id) {
        Pedido pedido = buscarPorId(id);
        pedidoRepository.delete(pedido);
    }

    public List<Pedido> buscarPorUsuario(Long usuarioId) {
        obtenerUsuario(usuarioId);
        return pedidoRepository.findByUsuarioId(usuarioId);
    }

    public List<Pedido> buscarPorEstado(String estado) {
        if (estado == null || estado.isBlank()) {
            throw new IllegalArgumentException("El estado de búsqueda es obligatorio");
        }
        return pedidoRepository.findByEstadoIgnoreCase(estado);
    }

    public List<DetallePedido> buscarDetalles(Long pedidoId) {
        buscarPorId(pedidoId);
        return detallePedidoRepository.buscarPorPedidoId(pedidoId);
    }

    public BigDecimal calcularTotalPorEstado(String estado) {
        if (estado == null || estado.isBlank()) {
            throw new IllegalArgumentException("El estado es obligatorio");
        }
        BigDecimal total = pedidoRepository.sumarTotalPorEstado(estado);
        return total != null ? total : BigDecimal.ZERO;
    }

    @Transactional
    public Pedido procesarPedido(ProcesarPedidoRequest solicitud) {
        validarSolicitud(solicitud);
        Usuario usuario = obtenerClienteActivo(solicitud.getUsuarioId());

        Pedido pedido = new Pedido();
        pedido.setUsuario(usuario);
        pedido.setTelefono(usuario.getTelefono());
        pedido.setFecha(LocalDateTime.now());
        pedido.setFechaActualizacion(LocalDateTime.now());
        pedido.setEstado("PENDIENTE");
        pedido.setSubtotal(BigDecimal.ZERO);
        BigDecimal costoEntrega = solicitud.getCostoEntrega() == null
                ? BigDecimal.ZERO : solicitud.getCostoEntrega();
        pedido.setCostoEntrega(costoEntrega);
        pedido.setTotal(BigDecimal.ZERO);
        pedido.setTipoEntrega(solicitud.getTipoEntrega());
        pedido.setDireccionEntrega(solicitud.getDireccionEntrega());
        completarDireccion(pedido, usuario);
        if ("DOMICILIO".equalsIgnoreCase(pedido.getTipoEntrega())
                && (pedido.getDireccionEntrega() == null || pedido.getDireccionEntrega().isBlank())) {
            throw new IllegalArgumentException("El usuario debe tener una dirección para pedidos a domicilio");
        }
        pedido.setObservaciones(solicitud.getObservaciones());
        pedido = pedidoRepository.save(pedido);

        BigDecimal total = BigDecimal.ZERO;
        for (DetallePedidoRequest datosDetalle : solicitud.getDetalles()) {
            if (datosDetalle.getProductoId() == null || datosDetalle.getCantidad() <= 0) {
                throw new IllegalArgumentException("Cada detalle debe tener producto y cantidad mayor que cero");
            }

            Producto producto = productoRepository.findById(datosDetalle.getProductoId())
                    .orElseThrow(() -> new RecursoNoEncontradoException(
                            "Producto no encontrado: " + datosDetalle.getProductoId()));

            BigDecimal subtotal = producto.getPrecio()
                    .multiply(BigDecimal.valueOf(datosDetalle.getCantidad()));

            DetallePedido detalle = new DetallePedido();
            detalle.setPedido(pedido);
            detalle.setProducto(producto);
            detalle.setCantidad(datosDetalle.getCantidad());
            detalle.setPrecioUnitario(producto.getPrecio());
            detalle.setSubtotal(subtotal);
            detallePedidoRepository.save(detalle);

            total = total.add(subtotal);
        }

        pedido.setSubtotal(total);
        pedido.setTotal(total.add(costoEntrega));
        pedido.setFechaActualizacion(LocalDateTime.now());
        return pedidoRepository.save(pedido);
    }

    private void validar(Pedido pedido) {
        if (pedido.getUsuario() == null || pedido.getUsuario().getId() == null) {
            throw new IllegalArgumentException("El usuario del pedido es obligatorio");
        }
        if (pedido.getEstado() == null || pedido.getEstado().isBlank()) {
            throw new IllegalArgumentException("El estado del pedido es obligatorio");
        }
        if (pedido.getTotal() == null || pedido.getTotal().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El total del pedido debe ser mayor que cero");
        }
        if (pedido.getSubtotal() == null || pedido.getSubtotal().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("El subtotal del pedido no puede ser negativo");
        }
        if (pedido.getCostoEntrega() == null || pedido.getCostoEntrega().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("El costo de entrega no puede ser negativo");
        }
        if (pedido.getTipoEntrega() == null || pedido.getTipoEntrega().isBlank()) {
            throw new IllegalArgumentException("El tipo de entrega es obligatorio");
        }
        if ("DOMICILIO".equalsIgnoreCase(pedido.getTipoEntrega())
                && (pedido.getDireccionEntrega() == null || pedido.getDireccionEntrega().isBlank())) {
            throw new IllegalArgumentException("La dirección de entrega es obligatoria para pedidos a domicilio");
        }
    }

    private Usuario obtenerUsuario(Long usuarioId) {
        if (usuarioId == null) {
            throw new RecursoNoEncontradoException("Usuario no encontrado: null");
        }
        return usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado: " + usuarioId));
    }

    private Usuario obtenerClienteActivo(Long usuarioId) {
        Usuario usuario = obtenerUsuario(usuarioId);
        if (!Boolean.TRUE.equals(usuario.getActivo())) {
            throw new ReglaNegocioException("El usuario está inactivo");
        }
        if (!"CLIENTE".equalsIgnoreCase(usuario.getRol())) {
            throw new ReglaNegocioException("Solo los usuarios con rol CLIENTE pueden realizar pedidos");
        }
        return usuario;
    }

    private void validarSolicitud(ProcesarPedidoRequest solicitud) {
        if (solicitud == null) {
            throw new IllegalArgumentException("Los datos del pedido son obligatorios");
        }
        if (solicitud.getUsuarioId() == null) {
            throw new IllegalArgumentException("El usuario del pedido es obligatorio");
        }
        if (solicitud.getTipoEntrega() == null || solicitud.getTipoEntrega().isBlank()) {
            throw new IllegalArgumentException("El tipo de entrega es obligatorio");
        }
        if (solicitud.getDetalles() == null || solicitud.getDetalles().isEmpty()) {
            throw new IllegalArgumentException("El pedido debe contener al menos un producto");
        }
        if (solicitud.getCostoEntrega() != null
                && solicitud.getCostoEntrega().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("El costo de entrega no puede ser negativo");
        }
    }

    private void completarDatosEconomicos(Pedido pedido) {
        if (pedido.getCostoEntrega() == null) {
            pedido.setCostoEntrega(BigDecimal.ZERO);
        }
        if (pedido.getSubtotal() == null && pedido.getTotal() != null) {
            pedido.setSubtotal(pedido.getTotal().subtract(pedido.getCostoEntrega()));
        }
        if (pedido.getTotal() == null && pedido.getSubtotal() != null) {
            pedido.setTotal(pedido.getSubtotal().add(pedido.getCostoEntrega()));
        }
    }

    private void completarDireccion(Pedido pedido, Usuario usuario) {
        if ("DOMICILIO".equalsIgnoreCase(pedido.getTipoEntrega())
                && (pedido.getDireccionEntrega() == null || pedido.getDireccionEntrega().isBlank())) {
            pedido.setDireccionEntrega(usuario.getDireccion());
        }
    }
}
