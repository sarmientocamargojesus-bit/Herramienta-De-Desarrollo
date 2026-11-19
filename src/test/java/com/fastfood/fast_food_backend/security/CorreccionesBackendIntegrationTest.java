package com.fastfood.fast_food_backend.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import com.fastfood.fast_food_backend.dto.DetallePedidoRequest;
import com.fastfood.fast_food_backend.dto.ProcesarPedidoRequest;
import com.fastfood.fast_food_backend.entities.CarritoItem;
import com.fastfood.fast_food_backend.entities.Categoria;
import com.fastfood.fast_food_backend.entities.Pago;
import com.fastfood.fast_food_backend.entities.Pedido;
import com.fastfood.fast_food_backend.entities.Producto;
import com.fastfood.fast_food_backend.entities.Usuario;
import com.fastfood.fast_food_backend.exceptions.ReglaNegocioException;
import com.fastfood.fast_food_backend.repositories.CarritoItemRepository;
import com.fastfood.fast_food_backend.repositories.CategoriaRepository;
import com.fastfood.fast_food_backend.repositories.PedidoRepository;
import com.fastfood.fast_food_backend.repositories.ProductoRepository;
import com.fastfood.fast_food_backend.repositories.UsuarioRepository;
import com.fastfood.fast_food_backend.services.PagoService;
import com.fastfood.fast_food_backend.services.PedidoService;
import com.fastfood.fast_food_backend.services.UsuarioService;
import com.jayway.jsonpath.JsonPath;

/** Cubre las correcciones de la revisión del backend: estados, administradores, borrados y validaciones. */
@SpringBootTest
@AutoConfigureMockMvc
class CorreccionesBackendIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private JdbcTemplate jdbc;
    @Autowired private UsuarioRepository usuarios;
    @Autowired private ProductoRepository productos;
    @Autowired private PedidoRepository pedidos;
    @Autowired private CategoriaRepository categorias;
    @Autowired private CarritoItemRepository carrito;
    @Autowired private PedidoService pedidoService;
    @Autowired private PagoService pagoService;
    @Autowired private UsuarioService usuarioService;
    @Autowired private PasswordEncoder passwordEncoder;

    private Usuario cliente;
    private Usuario admin;
    private Producto producto;
    private String tokenCliente;
    private String tokenEmpleado;
    private String tokenAdmin;

    @BeforeEach
    void preparar() throws Exception {
        for (String tabla : List.of("pagos", "carrito_items", "detalles_pedido", "imagenes_producto",
                "inventarios", "pedidos", "productos", "usuarios")) {
            jdbc.update("DELETE FROM " + tabla);
        }
        jdbc.update("DELETE FROM categorias WHERE nombre = 'PRUEBA_INACTIVA'");
        cliente = usuario("cliente.correcciones@fastfood.test", "CLIENTE");
        usuario("empleado.correcciones@fastfood.test", "EMPLEADO");
        admin = usuario("admin.correcciones@fastfood.test", "ADMINISTRADOR");
        producto = new Producto();
        producto.setNombre("Hamburguesa de prueba");
        producto.setPrecio(new BigDecimal("12.50"));
        producto.setStock(10);
        producto.setCategoria("HAMBURGUESAS");
        producto = productos.save(producto);
        tokenCliente = login(cliente.getCorreo());
        tokenEmpleado = login("empleado.correcciones@fastfood.test");
        tokenAdmin = login(admin.getCorreo());
    }

    // 1. Estado del pedido

    @Test
    void elEmpleadoAvanzaElPedidoPasoAPaso() throws Exception {
        Pedido pedido = pagado();
        for (String estado : List.of("EN_PREPARACION", "LISTO", "ENTREGADO")) {
            mockMvc.perform(put("/api/pedidos/" + pedido.getId() + "/estado")
                            .header("Authorization", bearer(tokenEmpleado))
                            .contentType(MediaType.APPLICATION_JSON).content("{\"estado\":\"" + estado + "\"}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.estado").value(estado));
        }
        mockMvc.perform(put("/api/pedidos/" + pedido.getId() + "/estado")
                        .header("Authorization", bearer(tokenEmpleado))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"estado\":\"LISTO\"}"))
                .andExpect(status().isConflict());
    }

    @Test
    void noSePuedeSaltarEstadosNiMoverUnPedidoPendiente() throws Exception {
        Pedido pagado = pagado();
        mockMvc.perform(put("/api/pedidos/" + pagado.getId() + "/estado")
                        .header("Authorization", bearer(tokenEmpleado))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"estado\":\"ENTREGADO\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.mensaje").value("Un pedido CONFIRMADO solo puede pasar a EN_PREPARACION"));

        Pedido pendiente = pendiente(2);
        assertThrows(ReglaNegocioException.class, () -> pedidoService.cambiarEstado(pendiente.getId(), "EN_PREPARACION"));
    }

    @Test
    void elClienteNoPuedeCambiarElEstado() throws Exception {
        Pedido pedido = pagado();
        mockMvc.perform(put("/api/pedidos/" + pedido.getId() + "/estado")
                        .header("Authorization", bearer(tokenCliente))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"estado\":\"EN_PREPARACION\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void anularElPagoDeUnPedidoEnPreparacionDevuelveElStockYUnoEntregadoNoSeAnula() {
        Pedido enPreparacion = pagado();
        pedidoService.cambiarEstado(enPreparacion.getId(), "EN_PREPARACION");
        assertEquals(8, stock());
        pagoService.eliminar(pagoService.buscarPorPedido(enPreparacion.getId()).getId());
        assertEquals(10, stock());
        assertEquals("CANCELADO", pedidos.findById(enPreparacion.getId()).orElseThrow().getEstado());

        Pedido entregado = pagado();
        pedidoService.cambiarEstado(entregado.getId(), "EN_PREPARACION");
        pedidoService.cambiarEstado(entregado.getId(), "LISTO");
        pedidoService.cambiarEstado(entregado.getId(), "ENTREGADO");
        Long pagoId = pagoService.buscarPorPedido(entregado.getId()).getId();
        assertThrows(ReglaNegocioException.class, () -> pagoService.eliminar(pagoId));
        assertEquals(8, stock());
    }

    // 2. Último administrador

    @Test
    void noSePuedeQuitarElRolNiDesactivarAlUnicoAdministrador() throws Exception {
        Usuario otroAdmin = usuario("otro.admin@fastfood.test", "ADMINISTRADOR");
        String tokenOtro = login(otroAdmin.getCorreo());
        // El administrador no puede quitarse el rol a sí mismo.
        mockMvc.perform(put("/api/usuarios/" + admin.getId() + "/rol").header("Authorization", bearer(tokenAdmin))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"rol\":\"CLIENTE\"}"))
                .andExpect(status().isConflict());
        // Con dos administradores, uno puede quitarle el rol al otro.
        mockMvc.perform(put("/api/usuarios/" + admin.getId() + "/rol").header("Authorization", bearer(tokenOtro))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"rol\":\"EMPLEADO\"}"))
                .andExpect(status().isOk());
        // Ahora es el único: no se le puede desactivar ni borrar.
        ReglaNegocioException error = assertThrows(ReglaNegocioException.class,
                () -> usuarioService.eliminar(otroAdmin.getId(), "alguien@fastfood.test"));
        assertTrue(error.getMessage().contains("al menos un administrador"));
    }

    // 4. y 5. Borrados con mensaje claro

    @Test
    void unProductoConPedidosNoSeBorraYUnoSoloEnCarritosSi() throws Exception {
        pendiente(1);
        mockMvc.perform(delete("/api/productos/" + producto.getId()).header("Authorization", bearer(tokenAdmin)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.mensaje").value(
                        "No se puede eliminar un producto que ya tiene pedidos; deja su stock en 0 para retirarlo"));

        Producto enCarrito = new Producto();
        enCarrito.setNombre("Gaseosa");
        enCarrito.setPrecio(new BigDecimal("3.00"));
        enCarrito.setStock(5);
        enCarrito.setCategoria("BEBIDAS");
        enCarrito = productos.save(enCarrito);
        CarritoItem item = new CarritoItem();
        item.setUsuario(cliente);
        item.setProducto(enCarrito);
        item.setCantidad(1);
        item.setFechaAgregado(LocalDateTime.now());
        carrito.save(item);

        mockMvc.perform(delete("/api/productos/" + enCarrito.getId()).header("Authorization", bearer(tokenAdmin)))
                .andExpect(status().isNoContent());
        assertFalse(productos.existsById(enCarrito.getId()));
        assertEquals(0, carrito.count());
    }

    @Test
    void soloSeEliminanPedidosPendientesOCancelados() throws Exception {
        Pedido pagado = pagado();
        mockMvc.perform(delete("/api/pedidos/" + pagado.getId()).header("Authorization", bearer(tokenEmpleado)))
                .andExpect(status().isConflict());
        Pedido pendiente = pendiente(1);
        mockMvc.perform(delete("/api/pedidos/" + pendiente.getId()).header("Authorization", bearer(tokenEmpleado)))
                .andExpect(status().isNoContent());
        assertTrue(pedidos.existsById(pagado.getId()));
    }

    // 6. Permisos de inventario

    @Test
    void elEmpleadoNoPuedeBorrarConfiguracionesDeInventario() throws Exception {
        mockMvc.perform(delete("/api/inventarios/1").header("Authorization", bearer(tokenEmpleado)))
                .andExpect(status().isForbidden());
    }

    // 7. Categoría inactiva

    @Test
    void noSePuedeAgregarAlCarritoUnProductoDeCategoriaInactiva() throws Exception {
        Categoria inactiva = new Categoria();
        inactiva.setNombre("PRUEBA_INACTIVA");
        inactiva.setActivo(false);
        inactiva = categorias.save(inactiva);
        producto.setCategoria("PRUEBA_INACTIVA");
        producto.setCategoriaReferencia(inactiva);
        productos.save(producto);

        mockMvc.perform(put("/api/carrito/productos/" + producto.getId()).header("Authorization", bearer(tokenCliente))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"cantidad\":1}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.mensaje").value("El producto no está disponible: Hamburguesa de prueba"));
    }

    // 9., 14., 15. y 16. Validaciones

    @Test
    void editarUnProductoSinEnviarStockConservaElStock() throws Exception {
        mockMvc.perform(put("/api/productos/" + producto.getId()).header("Authorization", bearer(tokenAdmin))
                        .contentType(MediaType.APPLICATION_JSON).content("""
                                {"nombre":"Hamburguesa editada","precio":13.00,"categoria":"HAMBURGUESAS"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stock").value(10));
        assertEquals(10, stock());
    }

    @Test
    void validaContrasenaCorreoYLargos() throws Exception {
        mockMvc.perform(put("/api/usuarios/" + cliente.getId()).header("Authorization", bearer(tokenAdmin))
                        .contentType(MediaType.APPLICATION_JSON).content("""
                                {"nombre":"Cliente","correo":"cliente.correcciones@fastfood.test",
                                 "telefono":"999111222","contrasena":"123"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("La contraseña debe tener al menos 6 caracteres"));

        mockMvc.perform(post("/api/usuarios").contentType(MediaType.APPLICATION_JSON).content("""
                                {"nombre":"Ana","correo":"ana@correo","telefono":"999111222","contrasena":"123456"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("El correo del usuario no es válido"));

        mockMvc.perform(post("/api/usuarios").contentType(MediaType.APPLICATION_JSON).content("""
                                {"nombre":"%s","correo":"ana@correo.com","telefono":"999111222","contrasena":"123456"}
                                """.formatted("A".repeat(101))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void unPagoNoAceptaFechasFuturas() {
        Pedido pedido = pagado();
        Pago pago = pagoService.buscarPorPedido(pedido.getId());
        Pago datos = new Pago();
        datos.setMetodo("YAPE");
        datos.setEstado("APROBADO");
        datos.setFecha(LocalDateTime.now().plusDays(3));
        assertThrows(IllegalArgumentException.class, () -> pagoService.actualizar(pago.getId(), datos));
    }

    // Utilidades

    private Pedido pendiente(int cantidad) {
        ProcesarPedidoRequest solicitud = new ProcesarPedidoRequest();
        solicitud.setUsuarioId(cliente.getId());
        solicitud.setTipoEntrega("RECOJO");
        DetallePedidoRequest linea = new DetallePedidoRequest();
        linea.setProductoId(producto.getId());
        linea.setCantidad(cantidad);
        solicitud.setDetalles(List.of(linea));
        return pedidoService.procesarPedido(solicitud);
    }

    private Pedido pagado() {
        Pedido pedido = pendiente(2);
        Pago pago = new Pago();
        pago.setPedido(pedido);
        pago.setMetodo("YAPE");
        pago.setMonto(pedido.getTotal());
        pagoService.registrar(pago);
        return pedidos.findById(pedido.getId()).orElseThrow();
    }

    private Usuario usuario(String correo, String rol) {
        Usuario usuario = new Usuario();
        usuario.setNombre("Usuario de prueba");
        usuario.setCorreo(correo);
        usuario.setContrasena(passwordEncoder.encode("123456"));
        usuario.setTelefono("999111222");
        usuario.setDireccion("Direccion original");
        usuario.setRol(rol);
        usuario.setActivo(true);
        return usuarios.save(usuario);
    }

    private String login(String correo) throws Exception {
        var resultado = mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"correo":"%s","contrasena":"123456"}
                                """.formatted(correo)))
                .andExpect(status().isOk()).andReturn();
        return JsonPath.read(resultado.getResponse().getContentAsString(), "$.token");
    }

    private int stock() { return productos.findById(producto.getId()).orElseThrow().getStock(); }
    private String bearer(String token) { return "Bearer " + token; }
}
