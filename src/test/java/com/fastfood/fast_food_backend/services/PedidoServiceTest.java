package com.fastfood.fast_food_backend.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fastfood.fast_food_backend.entities.Pedido;
import com.fastfood.fast_food_backend.entities.Usuario;
import com.fastfood.fast_food_backend.exceptions.RecursoNoEncontradoException;
import com.fastfood.fast_food_backend.repositories.UsuarioRepository;
import com.fastfood.fast_food_backend.repositories.DetallePedidoRepository;
import com.fastfood.fast_food_backend.repositories.PedidoRepository;
import com.fastfood.fast_food_backend.repositories.ProductoRepository;

@ExtendWith(MockitoExtension.class)
class PedidoServiceTest {

    @Mock
    private PedidoRepository pedidoRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private ProductoRepository productoRepository;

    @Mock
    private DetallePedidoRepository detallePedidoRepository;

    @InjectMocks
    private PedidoService pedidoService;

    @Test
    void debeRegistrarUnPedidoValido() {
        Pedido pedido = pedido();
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario()));
        when(pedidoRepository.save(pedido)).thenReturn(pedido);

        Pedido resultado = pedidoService.registrar(pedido);

        assertSame(pedido, resultado);
        assertNotNull(resultado.getFecha());
        verify(pedidoRepository).save(pedido);
    }

    @Test
    void debeRechazarPedidoDeUsuarioInexistente() {
        Pedido pedido = pedido();
        when(usuarioRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () -> pedidoService.registrar(pedido));

        verify(pedidoRepository, never()).save(any());
    }

    @Test
    void debeBuscarPedidosPorEstado() {
        Pedido pedido = pedido();
        when(pedidoRepository.findByEstadoIgnoreCase("PENDIENTE")).thenReturn(List.of(pedido));

        List<Pedido> resultado = pedidoService.buscarPorEstado("PENDIENTE");

        assertEquals(1, resultado.size());
        assertSame(pedido, resultado.get(0));
    }

    @Test
    void debeBuscarPedidosPorUsuario() {
        Pedido pedido = pedido();
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario()));
        when(pedidoRepository.findByUsuarioId(1L)).thenReturn(List.of(pedido));

        List<Pedido> resultado = pedidoService.buscarPorUsuario(1L);

        assertEquals(1, resultado.size());
        assertSame(pedido, resultado.get(0));
    }

    private Pedido pedido() {
        Pedido pedido = new Pedido();
        pedido.setUsuario(usuario());
        pedido.setEstado("PENDIENTE");
        pedido.setTotal(new BigDecimal("25.00"));
        pedido.setTipoEntrega("RECOJO");
        pedido.setObservaciones("Sin mayonesa");
        return pedido;
    }

    private Usuario usuario() {
        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setTelefono("999111222");
        usuario.setRol("CLIENTE");
        usuario.setActivo(true);
        return usuario;
    }
}
