package com.fastfood.fast_food_backend.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
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
import com.fastfood.fast_food_backend.entities.Pago;
import com.fastfood.fast_food_backend.entities.Pedido;
import com.fastfood.fast_food_backend.entities.Producto;
import com.fastfood.fast_food_backend.entities.Usuario;
import com.fastfood.fast_food_backend.exceptions.ReglaNegocioException;
import com.fastfood.fast_food_backend.repositories.PagoRepository;
import com.fastfood.fast_food_backend.repositories.PedidoRepository;
import com.fastfood.fast_food_backend.repositories.ProductoRepository;
import com.fastfood.fast_food_backend.repositories.UsuarioRepository;
import com.fastfood.fast_food_backend.services.PagoService;
import com.fastfood.fast_food_backend.services.PedidoService;
import com.jayway.jsonpath.JsonPath;

@SpringBootTest
@AutoConfigureMockMvc
class PagoIntegridadIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private JdbcTemplate jdbc;
    @Autowired private UsuarioRepository usuarios;
    @Autowired private ProductoRepository productos;
    @Autowired private PedidoRepository pedidos;
    @Autowired private PagoRepository pagos;
    @Autowired private PedidoService pedidoService;
    @Autowired private PagoService pagoService;
    @Autowired private PasswordEncoder passwordEncoder;

    private Usuario cliente;
    private Usuario otroCliente;

    @BeforeEach
    void preparar() {
        for (String tabla : List.of("pagos", "carrito_items", "detalles_pedido", "imagenes_producto",
                "inventarios", "pedidos", "productos", "usuarios")) {
            jdbc.update("DELETE FROM " + tabla);
        }
        cliente = usuario("cliente.pago@fastfood.test");
        otroCliente = usuario("otro.pago@fastfood.test");
    }

    // Se repite porque una carrera depende del momento en que coinciden las dos solicitudes.
    @RepeatedTest(5)
    void pagosSimultaneosNoVendenLaUltimaUnidadDosVeces() throws Exception {
        Producto producto = producto(1);
        Pedido primero = pendiente(cliente, producto, 1);
        Pedido segundo = pendiente(otroCliente, producto, 1);

        List<Object> resultados = pagarAlMismoTiempo(primero, segundo);

        long aprobados = resultados.stream().filter(Pago.class::isInstance).count();
        long rechazados = resultados.stream().filter(ReglaNegocioException.class::isInstance).count();
        assertEquals(1, aprobados, "Resultados: " + resultados);
        assertEquals(1, rechazados, "Resultados: " + resultados);
        assertEquals(0, productos.findById(producto.getId()).orElseThrow().getStock());
        assertEquals(1, pagos.count());
        assertEquals(1, pedidos.findByEstadoIgnoreCase("CONFIRMADO").size());
        assertEquals(1, pedidos.findByEstadoIgnoreCase("PENDIENTE").size());
    }

    @Test
    void pagosSimultaneosDelMismoPedidoSoloRegistranUnPago() throws Exception {
        Producto producto = producto(5);
        Pedido pedido = pendiente(cliente, producto, 2);

        List<Object> resultados = pagarAlMismoTiempo(pedido, pedido);

        assertEquals(1, resultados.stream().filter(Pago.class::isInstance).count(), "Resultados: " + resultados);
        assertEquals(1, pagos.count());
        assertEquals(3, productos.findById(producto.getId()).orElseThrow().getStock());
    }

    @Test
    void pagoConIdDeOtroPagoNoLoSobrescribe() throws Exception {
        Producto producto = producto(10);
        Pedido pedidoAjeno = pendiente(otroCliente, producto, 2);
        Pago pagoAjeno = pagoService.registrar(pago(pedidoAjeno));
        Pedido propio = pendiente(cliente, producto, 2);

        var resultado = mockMvc.perform(post("/api/pagos").header("Authorization", bearer(login(cliente)))
                        .contentType(MediaType.APPLICATION_JSON).content("""
                                {"id":%d,"pedido":{"id":%d},"metodo":"YAPE","monto":25,
                                 "estado":"RECHAZADO","fecha":"2000-01-01T00:00:00"}
                                """.formatted(pagoAjeno.getId(), propio.getId())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.estado").value("APROBADO"))
                .andExpect(jsonPath("$.pedido.id").value(propio.getId().intValue()))
                .andReturn();

        Number idNuevo = JsonPath.read(resultado.getResponse().getContentAsString(), "$.id");
        assertNotEquals(pagoAjeno.getId(), idNuevo.longValue());
        assertEquals(pedidoAjeno.getId(), pagos.findById(pagoAjeno.getId()).orElseThrow().getPedido().getId());
        assertEquals(pagoAjeno.getId(), pagos.findByPedidoId(pedidoAjeno.getId()).orElseThrow().getId());
        assertEquals(2, pagos.count());
        assertEquals(6, productos.findById(producto.getId()).orElseThrow().getStock());
    }

    private List<Object> pagarAlMismoTiempo(Pedido primero, Pedido segundo) throws Exception {
        ExecutorService hilos = Executors.newFixedThreadPool(2);
        CountDownLatch inicio = new CountDownLatch(1);
        try {
            List<Future<Object>> tareas = new ArrayList<>();
            for (Pedido pedido : List.of(primero, segundo)) {
                tareas.add(hilos.submit(() -> {
                    inicio.await();
                    try {
                        return pagoService.registrar(pago(pedido));
                    } catch (RuntimeException exception) {
                        return exception;
                    }
                }));
            }
            inicio.countDown();
            List<Object> resultados = new ArrayList<>();
            for (Future<Object> tarea : tareas) {
                resultados.add(tarea.get(30, TimeUnit.SECONDS));
            }
            return resultados;
        } finally {
            hilos.shutdownNow();
        }
    }

    private Pago pago(Pedido pedido) {
        Pedido referencia = new Pedido();
        referencia.setId(pedido.getId());
        Pago pago = new Pago();
        pago.setPedido(referencia);
        pago.setMetodo("YAPE");
        pago.setMonto(pedido.getTotal());
        return pago;
    }

    private Pedido pendiente(Usuario usuario, Producto producto, int cantidad) {
        ProcesarPedidoRequest solicitud = new ProcesarPedidoRequest();
        solicitud.setUsuarioId(usuario.getId());
        solicitud.setTipoEntrega("RECOJO");
        DetallePedidoRequest linea = new DetallePedidoRequest();
        linea.setProductoId(producto.getId());
        linea.setCantidad(cantidad);
        solicitud.setDetalles(List.of(linea));
        return pedidoService.procesarPedido(solicitud);
    }

    private Producto producto(int stock) {
        Producto producto = new Producto();
        producto.setNombre("Hamburguesa de prueba");
        producto.setPrecio(new BigDecimal("12.50"));
        producto.setStock(stock);
        producto.setCategoria("HAMBURGUESAS");
        return productos.save(producto);
    }

    private Usuario usuario(String correo) {
        Usuario usuario = new Usuario();
        usuario.setNombre("Usuario de prueba");
        usuario.setCorreo(correo);
        usuario.setContrasena(passwordEncoder.encode("123456"));
        usuario.setTelefono("999111222");
        usuario.setRol("CLIENTE");
        usuario.setActivo(true);
        return usuarios.save(usuario);
    }

    private String login(Usuario usuario) throws Exception {
        var resultado = mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"correo":"%s","contrasena":"123456"}
                                """.formatted(usuario.getCorreo())))
                .andExpect(status().isOk()).andReturn();
        return JsonPath.read(resultado.getResponse().getContentAsString(), "$.token");
    }

    private String bearer(String token) { return "Bearer " + token; }
}
