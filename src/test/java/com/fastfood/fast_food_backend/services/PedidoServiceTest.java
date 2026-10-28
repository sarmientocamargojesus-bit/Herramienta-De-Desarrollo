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
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fastfood.fast_food_backend.entities.Pedido;
import com.fastfood.fast_food_backend.entities.Usuario;
import com.fastfood.fast_food_backend.entities.Producto;
import com.fastfood.fast_food_backend.entities.DetallePedido;
import com.fastfood.fast_food_backend.dto.CrearPedidoRequest;
import com.fastfood.fast_food_backend.dto.DetallePedidoRequest;
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
        Producto producto = new Producto();
        producto.setId(2L);
        producto.setNombre("Hamburguesa");
        producto.setPrecio(new BigDecimal("12.50"));
        producto.setStock(10);
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario()));
        when(productoRepository.findById(2L)).thenReturn(Optional.of(producto));
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(invocacion -> invocacion.getArgument(0));

        Pedido resultado = pedidoService.registrar(solicitud());

        assertEquals("PENDIENTE", resultado.getEstado());
        assertEquals(new BigDecimal("25.00"), resultado.getTotal());
        assertEquals(BigDecimal.ZERO, resultado.getCostoEntrega());
        assertNotNull(resultado.getFecha());
        assertEquals(10, producto.getStock());
        ArgumentCaptor<DetallePedido> linea = ArgumentCaptor.forClass(DetallePedido.class);
        verify(detallePedidoRepository).save(linea.capture());
        assertEquals(2, linea.getValue().getCantidad());
        assertEquals(new BigDecimal("12.50"), linea.getValue().getPrecioUnitario());
    }

    @Test
    void debeRechazarPedidoDeUsuarioInexistente() {
        when(usuarioRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () -> pedidoService.registrar(solicitud()));

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

    private CrearPedidoRequest solicitud() {
        DetallePedidoRequest detalle = new DetallePedidoRequest();
        detalle.setProductoId(2L);
        detalle.setCantidad(2);
        return new CrearPedidoRequest(new CrearPedidoRequest.UsuarioReferencia(1L), null,
                "RECOJO", null, "Sin mayonesa", List.of(detalle));
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
