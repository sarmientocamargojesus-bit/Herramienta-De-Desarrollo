package com.fastfood.fast_food_backend.services;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.fastfood.fast_food_backend.entities.Pedido;
import com.fastfood.fast_food_backend.exceptions.RecursoNoEncontradoException;
import com.fastfood.fast_food_backend.repositories.ClienteRepository;
import com.fastfood.fast_food_backend.repositories.PedidoRepository;

@Service
public class PedidoService {

    private final PedidoRepository pedidoRepository;
    private final ClienteRepository clienteRepository;

    public PedidoService(PedidoRepository pedidoRepository, ClienteRepository clienteRepository) {
        this.pedidoRepository = pedidoRepository;
        this.clienteRepository = clienteRepository;
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
}
