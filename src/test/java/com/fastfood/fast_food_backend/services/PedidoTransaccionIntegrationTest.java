package com.fastfood.fast_food_backend.services;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.fastfood.fast_food_backend.dto.DetallePedidoRequest;
import com.fastfood.fast_food_backend.dto.ProcesarPedidoRequest;
import com.fastfood.fast_food_backend.entities.Usuario;
import com.fastfood.fast_food_backend.entities.Pedido;
import com.fastfood.fast_food_backend.entities.Producto;
import com.fastfood.fast_food_backend.repositories.UsuarioRepository;
import com.fastfood.fast_food_backend.repositories.DetallePedidoRepository;
import com.fastfood.fast_food_backend.repositories.PedidoRepository;
import com.fastfood.fast_food_backend.repositories.ProductoRepository;
import com.fastfood.fast_food_backend.repositories.PagoRepository;
import com.fastfood.fast_food_backend.repositories.InventarioRepository;

@SpringBootTest
class PedidoTransaccionIntegrationTest {

    @Autowired
    private PedidoService pedidoService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private PedidoRepository pedidoRepository;

    @Autowired
    private DetallePedidoRepository detallePedidoRepository;

    @Autowired
    private PagoRepository pagoRepository;

    @Autowired
    private InventarioRepository inventarioRepository;

    @BeforeEach
    void limpiarBaseDeDatos() {
        pagoRepository.deleteAll();
        inventarioRepository.deleteAll();
        detallePedidoRepository.deleteAll();
        pedidoRepository.deleteAll();
        productoRepository.deleteAll();
        usuarioRepository.deleteAll();
    }

    @Test
    void debeCrearPedidoPendienteSinDescontarStock() {
        Usuario usuario = usuarioRepository.save(usuario());
        Producto producto = productoRepository.save(producto("Hamburguesa", "12.50", 10));

        Pedido pedido = pedidoService.procesarPedido(solicitud(usuario.getId(), detalle(producto.getId(), 2)));

        assertEquals(new BigDecimal("25.00"), pedido.getTotal());
        assertEquals(1, pedidoRepository.count());
        assertEquals(1, detallePedidoRepository.count());
        assertEquals(1, detallePedidoRepository.buscarPorPedidoId(pedido.getId()).size());
        assertEquals(new BigDecimal("25.00"), pedidoRepository.sumarTotalPorEstado("PENDIENTE"));
        assertEquals("PENDIENTE", pedido.getEstado());
        assertEquals(10, productoRepository.findById(producto.getId()).orElseThrow().getStock());
    }

    @Test
    void debePermitirCrearVariosPedidosAunqueElStockSeValideAlPagar() {
        Usuario usuario = usuarioRepository.save(usuario());
        Producto producto = productoRepository.save(producto("Hamburguesa", "12.50", 1));

        Pedido primero = pedidoService.procesarPedido(
                solicitud(usuario.getId(), detalle(producto.getId(), 2)));
        Pedido segundo = pedidoService.procesarPedido(
                solicitud(usuario.getId(), detalle(producto.getId(), 3)));

        assertEquals("PENDIENTE", primero.getEstado());
        assertEquals("PENDIENTE", segundo.getEstado());
        assertEquals(2, pedidoRepository.count());
        assertEquals(2, detallePedidoRepository.count());
        assertEquals(1, productoRepository.findById(producto.getId()).orElseThrow().getStock());
    }

    private Usuario usuario() {
        Usuario usuario = new Usuario();
        usuario.setNombre("Ana Pérez");
        usuario.setTelefono("999111222");
        usuario.setCorreo("ana.transaccion@correo.com");
        usuario.setContrasena("hash-prueba");
        usuario.setDireccion("Av. Principal 123");
        usuario.setRol("CLIENTE");
        usuario.setActivo(true);
        return usuario;
    }

    private Producto producto(String nombre, String precio, int stock) {
        Producto producto = new Producto();
        producto.setNombre(nombre);
        producto.setPrecio(new BigDecimal(precio));
        producto.setStock(stock);
        producto.setCategoria("COMIDA");
        return producto;
    }

    private DetallePedidoRequest detalle(Long productoId, int cantidad) {
        DetallePedidoRequest detalle = new DetallePedidoRequest();
        detalle.setProductoId(productoId);
        detalle.setCantidad(cantidad);
        return detalle;
    }

    private ProcesarPedidoRequest solicitud(Long usuarioId, DetallePedidoRequest... detalles) {
        ProcesarPedidoRequest solicitud = new ProcesarPedidoRequest();
        solicitud.setUsuarioId(usuarioId);
        solicitud.setTipoEntrega("RECOJO");
        solicitud.setObservaciones("Prueba transaccional");
        solicitud.setDetalles(List.of(detalles));
        return solicitud;
    }
}
