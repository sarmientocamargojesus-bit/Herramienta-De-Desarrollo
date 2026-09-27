package com.fastfood.fast_food_backend.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.fastfood.fast_food_backend.entities.DetallePedido;
import com.fastfood.fast_food_backend.entities.Inventario;
import com.fastfood.fast_food_backend.entities.Pago;
import com.fastfood.fast_food_backend.entities.Pedido;
import com.fastfood.fast_food_backend.entities.Producto;
import com.fastfood.fast_food_backend.entities.Usuario;
import com.fastfood.fast_food_backend.exceptions.ReglaNegocioException;
import com.fastfood.fast_food_backend.repositories.DetallePedidoRepository;
import com.fastfood.fast_food_backend.repositories.InventarioRepository;
import com.fastfood.fast_food_backend.repositories.PagoRepository;
import com.fastfood.fast_food_backend.repositories.PedidoRepository;
import com.fastfood.fast_food_backend.repositories.ProductoRepository;
import com.fastfood.fast_food_backend.repositories.UsuarioRepository;

@SpringBootTest
class NuevasEntidadesIntegrationTest {

    @Autowired private UsuarioService usuarioService;
    @Autowired private InventarioService inventarioService;
    @Autowired private PagoService pagoService;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private InventarioRepository inventarioRepository;
    @Autowired private PagoRepository pagoRepository;
    @Autowired private DetallePedidoRepository detallePedidoRepository;
    @Autowired private PedidoRepository pedidoRepository;
    @Autowired private ProductoRepository productoRepository;

    @BeforeEach
    void limpiarDatos() {
        pagoRepository.deleteAll();
        inventarioRepository.deleteAll();
        detallePedidoRepository.deleteAll();
        pedidoRepository.deleteAll();
        productoRepository.deleteAll();
        usuarioRepository.deleteAll();
    }

    @Test
    void debeGuardarLaContrasenaDelUsuarioConHash() {
        Usuario usuario = new Usuario();
        usuario.setNombre("Administrador");
        usuario.setCorreo("admin@fastfood.com");
        usuario.setContrasena("claveSegura123");
        usuario.setTelefono("999888777");
        usuario.setRol("ADMINISTRADOR");

        Usuario guardado = usuarioService.registrar(usuario);
        String hash = usuarioRepository.findById(guardado.getId()).orElseThrow().getContrasena();

        assertFalse(hash.equals("claveSegura123"));
        assertTrue(hash.startsWith("$2"));
        assertEquals("CLIENTE", guardado.getRol());

        Usuario promovido = usuarioService.cambiarRol(guardado.getId(), "ADMINISTRADOR");
        assertEquals("ADMINISTRADOR", promovido.getRol());
    }

    @Test
    void debeDetectarUnProductoConStockBajo() {
        Producto producto = productoRepository.save(producto(3));
        Inventario inventario = new Inventario();
        inventario.setProducto(producto);
        inventario.setStockMinimo(5);

        inventarioService.registrar(inventario);

        assertEquals(1, inventarioService.listarStockBajo().size());
    }

    @Test
    void debeRegistrarUnPagoSimuladoParaElPedido() {
        Usuario usuario = usuarioRepository.save(usuarioCliente());
        Producto producto = productoRepository.save(producto(10));
        Pedido pedido = pedidoRepository.save(pedido(usuario));
        guardarDetalle(pedido, producto, 2);
        Pago pago = new Pago();
        pago.setPedido(pedido);
        pago.setMetodo("YAPE");
        pago.setMonto(new BigDecimal("25.00"));

        Pago guardado = pagoService.registrar(pago);

        assertEquals(pedido.getId(), guardado.getPedido().getId());
        assertEquals("YAPE", guardado.getMetodo());
        assertEquals("APROBADO", guardado.getEstado());
        assertEquals("CONFIRMADO", pedidoRepository.findById(pedido.getId()).orElseThrow().getEstado());
        assertEquals(8, productoRepository.findById(producto.getId()).orElseThrow().getStock());
    }

    @Test
    void debeRevertirElPagoCuandoNoExisteStockSuficiente() {
        Usuario usuario = usuarioRepository.save(usuarioCliente());
        Producto producto = productoRepository.save(producto(1));
        Pedido pedido = pedidoRepository.save(pedido(usuario));
        guardarDetalle(pedido, producto, 2);
        Pago pago = new Pago();
        pago.setPedido(pedido);
        pago.setMetodo("YAPE");
        pago.setMonto(new BigDecimal("25.00"));

        assertThrows(ReglaNegocioException.class, () -> pagoService.registrar(pago));

        assertEquals(0, pagoRepository.count());
        assertEquals("PENDIENTE", pedidoRepository.findById(pedido.getId()).orElseThrow().getEstado());
        assertEquals(1, productoRepository.findById(producto.getId()).orElseThrow().getStock());
    }

    private Producto producto(int stock) {
        Producto producto = new Producto();
        producto.setNombre("Hamburguesa de prueba");
        producto.setDescripcion("Producto para inventario");
        producto.setPrecio(new BigDecimal("12.50"));
        producto.setStock(stock);
        producto.setCategoria("HAMBURGUESAS");
        return producto;
    }

    private Usuario usuarioCliente() {
        Usuario usuario = new Usuario();
        usuario.setNombre("Ana Pérez");
        usuario.setTelefono("999111222");
        usuario.setCorreo("ana.pago@correo.com");
        usuario.setContrasena("hash-prueba");
        usuario.setDireccion("Av. Principal 123");
        usuario.setRol("CLIENTE");
        usuario.setActivo(true);
        return usuario;
    }

    private Pedido pedido(Usuario usuario) {
        Pedido pedido = new Pedido();
        pedido.setUsuario(usuario);
        pedido.setTelefono("999111222");
        pedido.setFecha(LocalDateTime.now());
        pedido.setFechaActualizacion(LocalDateTime.now());
        pedido.setEstado("PENDIENTE");
        pedido.setSubtotal(new BigDecimal("25.00"));
        pedido.setCostoEntrega(BigDecimal.ZERO);
        pedido.setTotal(new BigDecimal("25.00"));
        pedido.setTipoEntrega("RECOJO");
        return pedido;
    }

    private void guardarDetalle(Pedido pedido, Producto producto, int cantidad) {
        DetallePedido detalle = new DetallePedido();
        detalle.setPedido(pedido);
        detalle.setProducto(producto);
        detalle.setCantidad(cantidad);
        detalle.setPrecioUnitario(producto.getPrecio());
        detalle.setSubtotal(producto.getPrecio().multiply(BigDecimal.valueOf(cantidad)));
        detallePedidoRepository.save(detalle);
    }
}
