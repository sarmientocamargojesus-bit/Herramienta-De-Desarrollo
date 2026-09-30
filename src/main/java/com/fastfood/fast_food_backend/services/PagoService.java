package com.fastfood.fast_food_backend.services;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fastfood.fast_food_backend.entities.DetallePedido;
import com.fastfood.fast_food_backend.entities.Pago;
import com.fastfood.fast_food_backend.entities.Pedido;
import com.fastfood.fast_food_backend.entities.Producto;
import com.fastfood.fast_food_backend.exceptions.RecursoNoEncontradoException;
import com.fastfood.fast_food_backend.exceptions.ReglaNegocioException;
import com.fastfood.fast_food_backend.repositories.DetallePedidoRepository;
import com.fastfood.fast_food_backend.repositories.PagoRepository;
import com.fastfood.fast_food_backend.repositories.PedidoRepository;
import com.fastfood.fast_food_backend.repositories.ProductoRepository;

@Service
public class PagoService {

    private final PagoRepository pagoRepository;
    private final PedidoRepository pedidoRepository;
    private final DetallePedidoRepository detallePedidoRepository;
    private final ProductoRepository productoRepository;
    private final InventarioService inventarioService;

    public PagoService(
            PagoRepository pagoRepository,
            PedidoRepository pedidoRepository,
            DetallePedidoRepository detallePedidoRepository,
            ProductoRepository productoRepository,
            InventarioService inventarioService) {
        this.pagoRepository = pagoRepository;
        this.pedidoRepository = pedidoRepository;
        this.detallePedidoRepository = detallePedidoRepository;
        this.productoRepository = productoRepository;
        this.inventarioService = inventarioService;
    }

    public List<Pago> listar() {
        return pagoRepository.findAll();
    }

    public Pago buscarPorId(Long id) {
        return pagoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Pago no encontrado: " + id));
    }

    public Pago buscarPorPedido(Long pedidoId) {
        return pagoRepository.findByPedidoId(pedidoId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Pago no encontrado para el pedido: " + pedidoId));
    }

    @Transactional
    public Pago registrar(Pago pago) {
        validar(pago);
        Long pedidoId = pago.getPedido().getId();
        if (pagoRepository.existsByPedidoId(pedidoId)) {
            throw new ReglaNegocioException("El pedido ya tiene un pago registrado");
        }
        Pedido pedido = buscarPedido(pedidoId);
        if (!"PENDIENTE".equalsIgnoreCase(pedido.getEstado())) {
            throw new ReglaNegocioException("Solo se pueden pagar pedidos con estado PENDIENTE");
        }
        validarMonto(pago.getMonto(), pedido);

        List<DetallePedido> detalles = detallePedidoRepository.buscarPorPedidoId(pedidoId);
        if (detalles.isEmpty()) {
            throw new ReglaNegocioException("El pedido no contiene productos");
        }

        Map<Long, Integer> cantidades = new LinkedHashMap<>();
        Map<Long, Producto> productos = new LinkedHashMap<>();
        for (DetallePedido detalle : detalles) {
            Producto producto = detalle.getProducto();
            cantidades.merge(producto.getId(), detalle.getCantidad(), Integer::sum);
            productos.put(producto.getId(), producto);
        }

        for (Map.Entry<Long, Integer> entrada : cantidades.entrySet()) {
            Producto producto = productos.get(entrada.getKey());
            if (producto.getStock() < entrada.getValue()) {
                throw new ReglaNegocioException(
                        "Stock insuficiente para el producto " + producto.getNombre());
            }
        }

        for (Map.Entry<Long, Integer> entrada : cantidades.entrySet()) {
            Producto producto = productos.get(entrada.getKey());
            int stockAnterior = producto.getStock();
            producto.setStock(stockAnterior - entrada.getValue());
            productoRepository.save(producto);
            inventarioService.registrarCambioDeStock(producto, stockAnterior);
        }

        pedido.setEstado("CONFIRMADO");
        pedido.setFechaActualizacion(LocalDateTime.now());
        pedidoRepository.save(pedido);

        pago.setPedido(pedido);
        pago.setMetodo(pago.getMetodo().trim().toUpperCase());
        pago.setEstado("APROBADO");
        pago.setFecha(LocalDateTime.now());
        return pagoRepository.save(pago);
    }

    public Pago actualizar(Long id, Pago datos) {
        Pago pago = buscarPorId(id);
        if (datos.getMetodo() == null || datos.getMetodo().isBlank()
                || datos.getEstado() == null || datos.getEstado().isBlank()) {
            throw new IllegalArgumentException("El método y el estado del pago son obligatorios");
        }
        BigDecimal monto = datos.getMonto() == null ? pago.getMonto() : datos.getMonto();
        validarMonto(monto, pago.getPedido());
        pago.setMetodo(datos.getMetodo().trim().toUpperCase());
        pago.setEstado(datos.getEstado().trim().toUpperCase());
        pago.setMonto(monto);
        pago.setFecha(datos.getFecha() == null ? pago.getFecha() : datos.getFecha());
        return pagoRepository.save(pago);
    }

    public void eliminar(Long id) {
        pagoRepository.delete(buscarPorId(id));
    }

    private void validar(Pago pago) {
        if (pago == null || pago.getPedido() == null || pago.getPedido().getId() == null) {
            throw new IllegalArgumentException("El pedido del pago es obligatorio");
        }
        if (pago.getMetodo() == null || pago.getMetodo().isBlank()) {
            throw new IllegalArgumentException("El método de pago es obligatorio");
        }
        if (pago.getMonto() == null || pago.getMonto().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El monto del pago debe ser mayor que cero");
        }
    }

    private Pedido buscarPedido(Long id) {
        return pedidoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Pedido no encontrado: " + id));
    }

    private void validarMonto(BigDecimal monto, Pedido pedido) {
        if (monto == null || monto.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El monto del pago debe ser mayor que cero");
        }
        if (pedido.getTotal() != null && monto.compareTo(pedido.getTotal()) != 0) {
            throw new ReglaNegocioException("El monto del pago debe coincidir con el total del pedido");
        }
    }
}
