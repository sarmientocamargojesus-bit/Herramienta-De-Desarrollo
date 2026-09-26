package com.fastfood.fast_food_backend.services;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fastfood.fast_food_backend.dto.DetallePedidoRequest;
import com.fastfood.fast_food_backend.dto.ProcesarPedidoRequest;
import com.fastfood.fast_food_backend.entities.DetallePedido;
import com.fastfood.fast_food_backend.entities.Pedido;
import com.fastfood.fast_food_backend.entities.Producto;
import com.fastfood.fast_food_backend.exceptions.RecursoNoEncontradoException;
import com.fastfood.fast_food_backend.exceptions.ReglaNegocioException;
import com.fastfood.fast_food_backend.repositories.ClienteRepository;
import com.fastfood.fast_food_backend.repositories.DetallePedidoRepository;
import com.fastfood.fast_food_backend.repositories.PedidoRepository;
import com.fastfood.fast_food_backend.repositories.ProductoRepository;

@Service
public class PedidoService {

    private final PedidoRepository pedidoRepository;
    private final ClienteRepository clienteRepository;
    private final ProductoRepository productoRepository;
    private final DetallePedidoRepository detallePedidoRepository;

    public PedidoService(
            PedidoRepository pedidoRepository,
            ClienteRepository clienteRepository,
            ProductoRepository productoRepository,
            DetallePedidoRepository detallePedidoRepository) {
        this.pedidoRepository = pedidoRepository;
        this.clienteRepository = clienteRepository;
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
        validar(pedido);
        validarCliente(pedido.getClienteId());
        if (pedido.getFecha() == null) {
            pedido.setFecha(LocalDateTime.now());
        }
        return pedidoRepository.save(pedido);
    }

    public Pedido actualizar(Long id, Pedido datosPedido) {
        Pedido pedido = buscarPorId(id);
        validar(datosPedido);
        validarCliente(datosPedido.getClienteId());

        pedido.setClienteId(datosPedido.getClienteId());
        pedido.setEstado(datosPedido.getEstado());
        pedido.setTotal(datosPedido.getTotal());
        pedido.setTipoEntrega(datosPedido.getTipoEntrega());
        pedido.setDireccionEntrega(datosPedido.getDireccionEntrega());
        pedido.setObservaciones(datosPedido.getObservaciones());
        if (datosPedido.getFecha() != null) {
            pedido.setFecha(datosPedido.getFecha());
        }
        return pedidoRepository.save(pedido);
    }

    public void eliminar(Long id) {
        Pedido pedido = buscarPorId(id);
        pedidoRepository.delete(pedido);
    }

    public List<Pedido> buscarPorCliente(Long clienteId) {
        validarCliente(clienteId);
        return pedidoRepository.findByClienteId(clienteId);
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
        validarCliente(solicitud.getClienteId());

        Pedido pedido = new Pedido();
        pedido.setClienteId(solicitud.getClienteId());
        pedido.setFecha(LocalDateTime.now());
        pedido.setEstado("PENDIENTE");
        pedido.setTotal(BigDecimal.ZERO);
        pedido.setTipoEntrega(solicitud.getTipoEntrega());
        pedido.setDireccionEntrega(solicitud.getDireccionEntrega());
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

            if (producto.getStock() < datosDetalle.getCantidad()) {
                throw new ReglaNegocioException(
                        "Stock insuficiente para el producto " + producto.getNombre());
            }

            producto.setStock(producto.getStock() - datosDetalle.getCantidad());
            productoRepository.save(producto);

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

        pedido.setTotal(total);
        return pedidoRepository.save(pedido);
    }

    private void validar(Pedido pedido) {
        if (pedido.getClienteId() == null) {
            throw new IllegalArgumentException("El cliente del pedido es obligatorio");
        }
        if (pedido.getEstado() == null || pedido.getEstado().isBlank()) {
            throw new IllegalArgumentException("El estado del pedido es obligatorio");
        }
        if (pedido.getTotal() == null || pedido.getTotal().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El total del pedido debe ser mayor que cero");
        }
        if (pedido.getTipoEntrega() == null || pedido.getTipoEntrega().isBlank()) {
            throw new IllegalArgumentException("El tipo de entrega es obligatorio");
        }
        if ("DOMICILIO".equalsIgnoreCase(pedido.getTipoEntrega())
                && (pedido.getDireccionEntrega() == null || pedido.getDireccionEntrega().isBlank())) {
            throw new IllegalArgumentException("La dirección de entrega es obligatoria para pedidos a domicilio");
        }
    }

    private void validarCliente(Long clienteId) {
        if (clienteId == null || !clienteRepository.existsById(clienteId)) {
            throw new RecursoNoEncontradoException("Cliente no encontrado: " + clienteId);
        }
    }

    private void validarSolicitud(ProcesarPedidoRequest solicitud) {
        if (solicitud == null) {
            throw new IllegalArgumentException("Los datos del pedido son obligatorios");
        }
        if (solicitud.getClienteId() == null) {
            throw new IllegalArgumentException("El cliente del pedido es obligatorio");
        }
        if (solicitud.getTipoEntrega() == null || solicitud.getTipoEntrega().isBlank()) {
            throw new IllegalArgumentException("El tipo de entrega es obligatorio");
        }
        if ("DOMICILIO".equalsIgnoreCase(solicitud.getTipoEntrega())
                && (solicitud.getDireccionEntrega() == null || solicitud.getDireccionEntrega().isBlank())) {
            throw new IllegalArgumentException("La dirección de entrega es obligatoria para pedidos a domicilio");
        }
        if (solicitud.getDetalles() == null || solicitud.getDetalles().isEmpty()) {
            throw new IllegalArgumentException("El pedido debe contener al menos un producto");
        }
    }
}
