package com.fastfood.fast_food_backend.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import com.fastfood.fast_food_backend.dto.DetallePedidoRequest;
import com.fastfood.fast_food_backend.dto.ProcesarPedidoRequest;
import com.fastfood.fast_food_backend.entities.Pago;
import com.fastfood.fast_food_backend.entities.Pedido;
import com.fastfood.fast_food_backend.entities.Producto;
import com.fastfood.fast_food_backend.entities.Usuario;
import com.fastfood.fast_food_backend.repositories.DetallePedidoRepository;
import com.fastfood.fast_food_backend.repositories.PagoRepository;
import com.fastfood.fast_food_backend.repositories.PedidoRepository;
import com.fastfood.fast_food_backend.repositories.ProductoRepository;
import com.fastfood.fast_food_backend.repositories.UsuarioRepository;
import com.fastfood.fast_food_backend.services.PagoService;
import com.fastfood.fast_food_backend.services.PedidoService;
import com.jayway.jsonpath.JsonPath;

@SpringBootTest
@AutoConfigureMockMvc
class PedidoIntegridadIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private JdbcTemplate jdbc;
    @Autowired private UsuarioRepository usuarios;
    @Autowired private ProductoRepository productos;
    @Autowired private PedidoRepository pedidos;
    @Autowired private DetallePedidoRepository detalles;
    @Autowired private PagoRepository pagos;
    @Autowired private PedidoService pedidoService;
    @Autowired private PagoService pagoService;
    @Autowired private PasswordEncoder passwordEncoder;

    private Usuario cliente;
    private Usuario otroCliente;
    private Producto producto;
    private String tokenCliente;
    private String tokenAdmin;

    @BeforeEach
    void preparar() throws Exception {
        for (String tabla : List.of("pagos", "carrito_items", "detalles_pedido", "imagenes_producto",
                "inventarios", "pedidos", "productos", "usuarios")) {
            jdbc.update("DELETE FROM " + tabla);
        }
        cliente = usuario("cliente.integridad@fastfood.test", "CLIENTE");
        otroCliente = usuario("otro.integridad@fastfood.test", "CLIENTE");
        Usuario admin = usuario("admin.integridad@fastfood.test", "ADMINISTRADOR");
        producto = new Producto();
        producto.setNombre("Hamburguesa de prueba");
        producto.setPrecio(new BigDecimal("12.50"));
        producto.setStock(10);
        producto.setCategoria("HAMBURGUESAS");
        producto = productos.save(producto);
        tokenCliente = login(cliente.getCorreo());
        tokenAdmin = login(admin.getCorreo());
    }

    @Test
    void noPermiteCrearPedidoConfirmadoSinProductos() throws Exception {
        mockMvc.perform(post("/api/pedidos").header("Authorization", bearer(tokenCliente))
                        .contentType(MediaType.APPLICATION_JSON).content("""
                                {"usuario":{"id":%d},"estado":"CONFIRMADO","total":1,
                                 "subtotal":1,"costoEntrega":0,"tipoEntrega":"RECOJO"}
                                """.formatted(cliente.getId())))
                .andExpect(status().isBadRequest());
        assertEquals(0, pedidos.count());
        assertEquals(10, stock());
    }

    @Test
    void creacionCalculaImportesYDejaElPedidoPendiente() throws Exception {
        var resultado = mockMvc.perform(post("/api/pedidos").header("Authorization", bearer(tokenCliente))
                        .contentType(MediaType.APPLICATION_JSON).content(creacion(cliente.getId(), """
                                "estado":"CONFIRMADO","subtotal":0.01,"total":0.01,"costoEntrega":999,
                                "fecha":"2000-01-01T00:00:00",
                                """)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.estado").value("PENDIENTE"))
                .andExpect(jsonPath("$.subtotal").value(25.0))
                .andExpect(jsonPath("$.costoEntrega").value(0.0))
                .andExpect(jsonPath("$.total").value(25.0)).andReturn();
        Long id = idRespuesta(resultado.getResponse().getContentAsString());
        var lineas = detalles.buscarPorPedidoId(id);
        assertEquals(1, lineas.size());
        assertEquals(2, lineas.getFirst().getCantidad());
        assertEquals(0, new BigDecimal("12.50").compareTo(lineas.getFirst().getPrecioUnitario()));
        assertEquals(0, new BigDecimal("25.00").compareTo(lineas.getFirst().getSubtotal()));
        assertNotEquals(LocalDateTime.of(2000, 1, 1, 0, 0), pedidos.findById(id).orElseThrow().getFecha());
        assertEquals(0, pagos.count());
        assertEquals(10, stock());
    }

    @Test
    void creacionNoSobrescribeUnPedidoConElIdEnviado() throws Exception {
        Pedido original = pendiente(otroCliente);
        Long idOriginal = original.getId();
        var resultado = mockMvc.perform(post("/api/pedidos").header("Authorization", bearer(tokenCliente))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(creacion(cliente.getId(), "\"id\":" + idOriginal + ",")))
                .andExpect(status().isCreated()).andReturn();
        assertNotEquals(idOriginal, idRespuesta(resultado.getResponse().getContentAsString()));
        assertEquals(otroCliente.getId(), pedidos.findById(idOriginal).orElseThrow().getUsuario().getId());
        assertEquals(1, detalles.buscarPorPedidoId(idOriginal).size());
        assertEquals(2, pedidos.count());
    }

    @Test
    void clienteNoPuedeCrearPedidoParaOtraCuenta() throws Exception {
        mockMvc.perform(post("/api/pedidos").header("Authorization", bearer(tokenCliente))
                        .contentType(MediaType.APPLICATION_JSON).content(creacion(otroCliente.getId(), "")))
                .andExpect(status().isForbidden());
        assertEquals(0, pedidos.count());
    }

    @ParameterizedTest
    @ValueSource(strings = {"[]", "[null]", "[{\"productoId\":1,\"cantidad\":0}]"})
    void detallesInvalidosNoCreanPedido(String lineas) throws Exception {
        mockMvc.perform(post("/api/pedidos").header("Authorization", bearer(tokenCliente))
                        .contentType(MediaType.APPLICATION_JSON).content("""
                                {"usuario":{"id":%d},"tipoEntrega":"RECOJO","estado":"PENDIENTE",
                                 "subtotal":25,"total":25,"detalles":%s}
                                """.formatted(cliente.getId(), lineas)))
                .andExpect(status().isBadRequest());
        assertEquals(0, pedidos.count());
    }

    @Test
    void productoInexistenteReviertePedidoYDetalles() throws Exception {
        mockMvc.perform(post("/api/pedidos").header("Authorization", bearer(tokenCliente))
                        .contentType(MediaType.APPLICATION_JSON).content("""
                                {"usuario":{"id":%d},"tipoEntrega":"RECOJO","estado":"PENDIENTE",
                                 "subtotal":25,"total":25,"detalles":[
                                 {"productoId":%d,"cantidad":1},{"productoId":999999999,"cantidad":1}]}
                                """.formatted(cliente.getId(), producto.getId())))
                .andExpect(status().isNotFound());
        assertEquals(0, pedidos.count());
        assertEquals(0, detalles.count());
    }

    @Test
    void procesarTambienCalculaElCostoEnElServidor() throws Exception {
        mockMvc.perform(post("/api/pedidos/procesar").header("Authorization", bearer(tokenCliente))
                        .contentType(MediaType.APPLICATION_JSON).content("""
                                {"usuarioId":%d,"tipoEntrega":"DOMICILIO","direccionEntrega":"Direccion prueba",
                                 "costoEntrega":999,"total":1,"estado":"CONFIRMADO",
                                 "detalles":[{"productoId":%d,"cantidad":2}]}
                                """.formatted(cliente.getId(), producto.getId())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.estado").value("PENDIENTE"))
                .andExpect(jsonPath("$.costoEntrega").value(0.0))
                .andExpect(jsonPath("$.total").value(25.0));
    }

    @Test
    void noAceptaUnTipoDeEntregaInexistente() throws Exception {
        mockMvc.perform(post("/api/pedidos/procesar").header("Authorization", bearer(tokenCliente))
                        .contentType(MediaType.APPLICATION_JSON).content("""
                                {"usuarioId":%d,"tipoEntrega":"INEXISTENTE",
                                 "detalles":[{"productoId":%d,"cantidad":1}]}
                                """.formatted(cliente.getId(), producto.getId())))
                .andExpect(status().isBadRequest());
        assertEquals(0, pedidos.count());
    }

    @Test
    void edicionNoConfirmaNiCambiaImportesPropietarioFechaOProductos() throws Exception {
        Pedido original = pendiente(cliente);
        LocalDateTime fechaOriginal = pedidos.findById(original.getId()).orElseThrow().getFecha();
        mockMvc.perform(put("/api/pedidos/{id}", original.getId()).header("Authorization", bearer(tokenAdmin))
                        .contentType(MediaType.APPLICATION_JSON).content("""
                                {"id":999999,"usuario":{"id":%d},"estado":"CONFIRMADO",
                                 "subtotal":1,"total":1,"costoEntrega":999,"tipoEntrega":"RECOJO",
                                 "fecha":"2000-01-01T00:00:00","observaciones":"Sin cebolla",
                                 "detalles":[{"productoId":999999,"cantidad":100}]}
                                """.formatted(otroCliente.getId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("PENDIENTE"))
                .andExpect(jsonPath("$.usuario.id").value(cliente.getId().intValue()))
                .andExpect(jsonPath("$.total").value(25.0))
                .andExpect(jsonPath("$.costoEntrega").value(0.0))
                .andExpect(jsonPath("$.observaciones").value("Sin cebolla"));
        assertEquals(fechaOriginal, pedidos.findById(original.getId()).orElseThrow().getFecha());
        assertEquals(2, detalles.buscarPorPedidoId(original.getId()).getFirst().getCantidad());
        assertEquals(10, stock());
    }

    @Test
    void puedeEditarSoloDatosDeEntregaSinReenviarElPedidoCompleto() throws Exception {
        Pedido original = pendiente(cliente);
        mockMvc.perform(put("/api/pedidos/{id}", original.getId()).header("Authorization", bearer(tokenAdmin))
                        .contentType(MediaType.APPLICATION_JSON).content("""
                                {"tipoEntrega":"DOMICILIO","direccionEntrega":"Nueva direccion",
                                 "telefono":"999555666","observaciones":"Tocar el timbre"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.direccionEntrega").value("Nueva direccion"))
                .andExpect(jsonPath("$.telefono").value("999555666"))
                .andExpect(jsonPath("$.total").value(25.0))
                .andExpect(jsonPath("$.estado").value("PENDIENTE"));
    }

    @Test
    void edicionDePedidoPagadoConservaLaCompraYElPrecioHistorico() throws Exception {
        Pedido original = pendiente(cliente);
        Pago pago = new Pago();
        pago.setPedido(original);
        pago.setMetodo("YAPE");
        pago.setMonto(new BigDecimal("25.00"));
        pagoService.registrar(pago);
        Producto actualizado = productos.findById(producto.getId()).orElseThrow();
        assertEquals(8, actualizado.getStock());
        actualizado.setPrecio(new BigDecimal("99.00"));
        productos.save(actualizado);
        mockMvc.perform(put("/api/pedidos/{id}", original.getId()).header("Authorization", bearer(tokenAdmin))
                        .contentType(MediaType.APPLICATION_JSON).content("""
                                {"usuario":{"id":%d},"estado":"PENDIENTE","total":1,"subtotal":1,
                                 "costoEntrega":99,"tipoEntrega":"RECOJO","observaciones":"Nota de entrega"}
                                """.formatted(cliente.getId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("CONFIRMADO"))
                .andExpect(jsonPath("$.total").value(25.0));
        assertEquals(0, new BigDecimal("25.00").compareTo(pagos.findByPedidoId(original.getId()).orElseThrow().getMonto()));
        assertEquals(0, new BigDecimal("12.50").compareTo(detalles.buscarPorPedidoId(original.getId()).getFirst().getPrecioUnitario()));
        assertEquals(8, stock());
    }

    @Test
    void pedidoCreadoPorLaRutaAntiguaSeConfirmaMediantePago() throws Exception {
        var creado = mockMvc.perform(post("/api/pedidos").header("Authorization", bearer(tokenCliente))
                        .contentType(MediaType.APPLICATION_JSON).content(creacion(cliente.getId(), "")))
                .andExpect(status().isCreated()).andReturn();
        Long id = idRespuesta(creado.getResponse().getContentAsString());
        mockMvc.perform(post("/api/pagos").header("Authorization", bearer(tokenCliente))
                        .contentType(MediaType.APPLICATION_JSON).content("""
                                {"pedido":{"id":%d},"metodo":"YAPE","monto":25}
                                """.formatted(id)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.estado").value("APROBADO"))
                .andExpect(jsonPath("$.pedido.estado").value("CONFIRMADO"));
        assertEquals(8, stock());
    }

    private String creacion(Long usuarioId, String adicionales) {
        return """
                {"usuario":{"id":%d},"tipoEntrega":"RECOJO",%s
                 "detalles":[{"productoId":%d,"cantidad":2,"precioUnitario":0.01,"subtotal":0.01}]}
                """.formatted(usuarioId, adicionales, producto.getId());
    }

    private Pedido pendiente(Usuario usuario) {
        ProcesarPedidoRequest solicitud = new ProcesarPedidoRequest();
        solicitud.setUsuarioId(usuario.getId());
        solicitud.setTipoEntrega("RECOJO");
        DetallePedidoRequest linea = new DetallePedidoRequest();
        linea.setProductoId(producto.getId());
        linea.setCantidad(2);
        solicitud.setDetalles(List.of(linea));
        return pedidoService.procesarPedido(solicitud);
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
    private Long idRespuesta(String respuesta) {
        Number id = JsonPath.read(respuesta, "$.id");
        return id.longValue();
    }
    private String bearer(String token) { return "Bearer " + token; }
}
