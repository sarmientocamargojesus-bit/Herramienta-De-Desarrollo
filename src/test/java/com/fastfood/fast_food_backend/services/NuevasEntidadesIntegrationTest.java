package com.fastfood.fast_food_backend.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.fastfood.fast_food_backend.dto.RegistroUsuarioRequest;
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
    @Autowired private ProductoService productoService;
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
        RegistroUsuarioRequest solicitud = new RegistroUsuarioRequest(
                "Administrador", "admin@fastfood.com", "123456", "999888777", null, null);

        Usuario guardado = usuarioService.registrar(solicitud);
        String hash = usuarioRepository.findById(guardado.getId()).orElseThrow().getContrasena();

        assertFalse(hash.equals("123456"));
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
    void debeCrearInventarioYMarcarAgotadoAlRegistrarProductoSinStock() {
        Producto producto = productoService.registrar(producto(0));

        Inventario inventario = inventarioRepository.findByProductoId(producto.getId()).orElseThrow();
        assertEquals(0, inventario.getStockMinimo());
        assertNotNull(inventario.getFechaStockBajo());
        assertNotNull(inventario.getFechaAgotado());
    }

    @Test
    void debeRegistrarStockBajoAlConfigurarUnMinimoIndividual() {
        Producto producto = productoService.registrar(producto(3));
        Inventario inventario = inventarioRepository.findByProductoId(producto.getId()).orElseThrow();
        assertEquals(0, inventario.getStockMinimo());
        assertNull(inventario.getFechaStockBajo());

        Inventario nuevosDatos = new Inventario();
        nuevosDatos.setStockMinimo(5);
        Inventario actualizado = inventarioService.actualizar(inventario.getId(), nuevosDatos);

        assertEquals(5, actualizado.getStockMinimo());
        assertNotNull(actualizado.getFechaStockBajo());
        assertNull(actualizado.getFechaAgotado());
    }

    @Test
    void debeRegistrarFechasAlLlegarAlMinimoYAgotarseYLimpiarlasAlReponer() {
        Producto producto = productoRepository.save(producto(6));
        Inventario configuracion = new Inventario();
        configuracion.setProducto(producto);
        configuracion.setStockMinimo(5);
        Inventario inventario = inventarioService.registrar(configuracion);
        assertNull(inventario.getFechaStockBajo());
        assertNull(inventario.getFechaAgotado());

        productoService.descontarStock(producto.getId(), 1);
        Inventario bajo = inventarioRepository.findByProductoId(producto.getId()).orElseThrow();
        assertNotNull(bajo.getFechaStockBajo());
        assertNull(bajo.getFechaAgotado());
        LocalDateTime fechaBajo = bajo.getFechaStockBajo();

        productoService.descontarStock(producto.getId(), 5);
        Inventario agotado = inventarioRepository.findByProductoId(producto.getId()).orElseThrow();
        assertEquals(fechaBajo, agotado.getFechaStockBajo());
        assertNotNull(agotado.getFechaAgotado());

        Producto reposicion = productoRepository.findById(producto.getId()).orElseThrow();
        reposicion.setStock(8);
        productoService.actualizar(producto.getId(), reposicion);
        Inventario repuesto = inventarioRepository.findByProductoId(producto.getId()).orElseThrow();
        assertNull(repuesto.getFechaStockBajo());
        assertNull(repuesto.getFechaAgotado());
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
    void debeMarcarAgotamientoCuandoElPagoDescuentaLaUltimaUnidad() {
        Usuario usuario = usuarioRepository.save(usuarioCliente());
        Producto producto = productoRepository.save(producto(2));
        Inventario configuracion = new Inventario();
        configuracion.setProducto(producto);
        configuracion.setStockMinimo(1);
        inventarioService.registrar(configuracion);
        Pedido pedido = pedidoRepository.save(pedido(usuario));
        guardarDetalle(pedido, producto, 2);
        Pago pago = new Pago();
        pago.setPedido(pedido);
        pago.setMetodo("YAPE");
        pago.setMonto(new BigDecimal("25.00"));

        pagoService.registrar(pago);

        assertEquals(0, productoRepository.findById(producto.getId()).orElseThrow().getStock());
        Inventario inventario = inventarioRepository.findByProductoId(producto.getId()).orElseThrow();
        assertNotNull(inventario.getFechaStockBajo());
        assertNotNull(inventario.getFechaAgotado());
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

    @Test
    void debeRechazarPagoSiElMismoProductoSuperaElStockEntreDosDetalles() {
        Usuario usuario = usuarioRepository.save(usuarioCliente());
        Producto producto = productoRepository.save(producto(1));
        Pedido pedido = pedidoRepository.save(pedido(usuario));
        guardarDetalle(pedido, producto, 1);
        guardarDetalle(pedido, producto, 1);
        Pago pago = new Pago();
        pago.setPedido(pedido);
        pago.setMetodo("YAPE");
        pago.setMonto(new BigDecimal("25.00"));

        assertThrows(ReglaNegocioException.class, () -> pagoService.registrar(pago));
        assertEquals(1, productoRepository.findById(producto.getId()).orElseThrow().getStock());
        assertEquals(0, pagoRepository.count());
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
