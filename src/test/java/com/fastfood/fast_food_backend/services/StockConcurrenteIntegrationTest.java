package com.fastfood.fast_food_backend.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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

import com.fastfood.fast_food_backend.entities.Producto;
import com.fastfood.fast_food_backend.entities.Usuario;
import com.fastfood.fast_food_backend.exceptions.ReglaNegocioException;
import com.fastfood.fast_food_backend.repositories.ProductoRepository;
import com.fastfood.fast_food_backend.repositories.UsuarioRepository;
import com.jayway.jsonpath.JsonPath;

@SpringBootTest
@AutoConfigureMockMvc
class StockConcurrenteIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private JdbcTemplate jdbc;
    @Autowired private ProductoRepository productos;
    @Autowired private UsuarioRepository usuarios;
    @Autowired private ProductoService productoService;
    @Autowired private PasswordEncoder encoder;

    private String tokenAdmin;

    @BeforeEach
    void preparar() throws Exception {
        for (String tabla : List.of("pagos", "carrito_items", "detalles_pedido", "imagenes_producto",
                "inventarios", "pedidos", "productos", "usuarios")) {
            jdbc.update("DELETE FROM " + tabla);
        }
        tokenAdmin = iniciarSesionAdministrador();
    }

    @Test
    void edicionConStockDesactualizadoNoDevuelveUnidadesVendidas() throws Exception {
        Producto producto = productoService.registrar(producto(10));
        productoService.descontarStock(producto.getId(), 2); // Venta mientras el administrador editaba.

        mockMvc.perform(put("/api/productos/{id}", producto.getId()).param("stockAnterior", "10")
                        .header("Authorization", "Bearer " + tokenAdmin)
                        .contentType(MediaType.APPLICATION_JSON).content(cuerpo("14.00", 10)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.mensaje").value(
                        "El stock cambió mientras editabas: ahora hay 8. Revisa el valor y vuelve a guardar"));

        Producto guardado = productos.findById(producto.getId()).orElseThrow();
        assertEquals(8, guardado.getStock());
        assertEquals(0, new BigDecimal("12.50").compareTo(guardado.getPrecio()));
    }

    @Test
    void edicionConStockVigenteSeGuarda() throws Exception {
        Producto producto = productoService.registrar(producto(10));

        mockMvc.perform(put("/api/productos/{id}", producto.getId()).param("stockAnterior", "10")
                        .header("Authorization", "Bearer " + tokenAdmin)
                        .contentType(MediaType.APPLICATION_JSON).content(cuerpo("14.00", 12)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stock").value(12))
                .andExpect(jsonPath("$.precio").value(14.0));
    }

    @Test
    void edicionSinStockAnteriorConservaElComportamientoAnterior() throws Exception {
        Producto producto = productoService.registrar(producto(10));

        mockMvc.perform(put("/api/productos/{id}", producto.getId())
                        .header("Authorization", "Bearer " + tokenAdmin)
                        .contentType(MediaType.APPLICATION_JSON).content(cuerpo("12.50", 6)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stock").value(6));
    }

    // Se repite porque la carrera depende del momento en que coinciden las dos operaciones.
    @RepeatedTest(5)
    void descuentosSimultaneosNoVendenLaUltimaUnidadDosVeces() throws Exception {
        Producto producto = productoService.registrar(producto(1));
        ExecutorService hilos = Executors.newFixedThreadPool(2);
        CountDownLatch inicio = new CountDownLatch(1);
        try {
            List<Future<Object>> tareas = new ArrayList<>();
            for (int i = 0; i < 2; i++) {
                tareas.add(hilos.submit(() -> {
                    inicio.await();
                    try {
                        return productoService.descontarStock(producto.getId(), 1);
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
            assertEquals(1, resultados.stream().filter(Producto.class::isInstance).count(), "Resultados: " + resultados);
            assertEquals(1, resultados.stream().filter(ReglaNegocioException.class::isInstance).count(),
                    "Resultados: " + resultados);
            assertEquals(0, productos.findById(producto.getId()).orElseThrow().getStock());
        } finally {
            hilos.shutdownNow();
        }
    }

    private String cuerpo(String precio, int stock) {
        return """
                {"nombre":"Hamburguesa de prueba","descripcion":"","precio":%s,"stock":%d,"categoria":"HAMBURGUESAS"}
                """.formatted(precio, stock);
    }

    private Producto producto(int stock) {
        Producto producto = new Producto();
        producto.setNombre("Hamburguesa de prueba");
        producto.setPrecio(new BigDecimal("12.50"));
        producto.setStock(stock);
        producto.setCategoria("HAMBURGUESAS");
        return producto;
    }

    private String iniciarSesionAdministrador() throws Exception {
        Usuario usuario = new Usuario();
        usuario.setNombre("Administrador de stock");
        usuario.setCorreo("admin.stock@fastfood.test");
        usuario.setContrasena(encoder.encode("123456"));
        usuario.setTelefono("999111222");
        usuario.setRol("ADMINISTRADOR");
        usuario.setActivo(true);
        usuarios.save(usuario);
        var resultado = mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"correo\":\"admin.stock@fastfood.test\",\"contrasena\":\"123456\"}"))
                .andExpect(status().isOk()).andReturn();
        return JsonPath.read(resultado.getResponse().getContentAsString(), "$.token");
    }
}
