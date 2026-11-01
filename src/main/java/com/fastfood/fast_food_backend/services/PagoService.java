package com.fastfood.fast_food_backend.services;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

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

    // Mismos métodos simulados que acepta el checkout.
    private static final Set<String> METODOS_PERMITIDOS = Set.of("YAPE", "TARJETA");
    private static final String ESTADO_APROBADO = "APROBADO";

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
        // El bloqueo del pedido hace que dos pagos del mismo pedido se atiendan uno después del otro.
        Pedido pedido = pedidoRepository.bloquearPorId(pedidoId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Pedido no encontrado: " + pedidoId));
        if (pagoRepository.existsByPedidoId(pedidoId)) {
            throw new ReglaNegocioException("El pedido ya tiene un pago registrado");
        }
        if (!"PENDIENTE".equalsIgnoreCase(pedido.getEstado())) {
            throw new ReglaNegocioException("Solo se pueden pagar pedidos con estado PENDIENTE");
        }
        validarMonto(pago.getMonto(), pedido);

        // Igual que en el checkout: se bloquean los productos en orden estable antes de leer su stock,
        // para que dos pagos simultáneos no descuenten la misma unidad.
        Map<Long, Producto> productos = new LinkedHashMap<>();
        for (Long productoId : detallePedidoRepository.buscarProductoIdsPorPedidoId(pedidoId)) {
            productos.put(productoId, productoRepository.bloquearPorId(productoId)
                    .orElseThrow(() -> new RecursoNoEncontradoException("Producto no encontrado: " + productoId)));
        }

        List<DetallePedido> detalles = detallePedidoRepository.buscarPorPedidoId(pedidoId);
        if (detalles.isEmpty()) {
            throw new ReglaNegocioException("El pedido no contiene productos");
        }

        Map<Long, Integer> cantidades = new LinkedHashMap<>();
        for (DetallePedido detalle : detalles) {
            cantidades.merge(detalle.getProducto().getId(), detalle.getCantidad(), Integer::sum);
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

        // Siempre se crea un pago nuevo: un id enviado en el JSON no puede actualizar el pago de otro pedido.
        Pago nuevo = new Pago();
        nuevo.setPedido(pedido);
        nuevo.setMetodo(normalizarMetodo(pago.getMetodo()));
        nuevo.setEstado(ESTADO_APROBADO);
        nuevo.setMonto(pago.getMonto());
        nuevo.setFecha(LocalDateTime.now());
        return pagoRepository.save(nuevo);
    }

    public Pago actualizar(Long id, Pago datos) {
        Pago pago = buscarPorId(id);
        if (datos.getMetodo() == null || datos.getMetodo().isBlank()
                || datos.getEstado() == null || datos.getEstado().isBlank()) {
            throw new IllegalArgumentException("El método y el estado del pago son obligatorios");
        }
        String metodo = normalizarMetodo(datos.getMetodo());
        String estado = datos.getEstado().trim().toUpperCase(Locale.ROOT);
        if (!ESTADO_APROBADO.equals(estado)) {
            throw new IllegalArgumentException("El estado del pago simulado solo puede ser APROBADO");
        }
        BigDecimal monto = datos.getMonto() == null ? pago.getMonto() : datos.getMonto();
        validarMonto(monto, pago.getPedido());
        pago.setMetodo(metodo);
        pago.setEstado(estado);
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
        normalizarMetodo(pago.getMetodo());
        if (pago.getMonto() == null || pago.getMonto().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El monto del pago debe ser mayor que cero");
        }
    }

    private String normalizarMetodo(String metodo) {
        String normalizado = metodo.trim().toUpperCase(Locale.ROOT);
        if (!METODOS_PERMITIDOS.contains(normalizado)) {
            throw new IllegalArgumentException("El pago simulado solo admite TARJETA o YAPE");
        }
        return normalizado;
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
