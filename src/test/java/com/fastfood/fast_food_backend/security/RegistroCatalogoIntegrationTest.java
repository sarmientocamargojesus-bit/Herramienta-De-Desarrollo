package com.fastfood.fast_food_backend.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
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
import org.springframework.test.web.servlet.MvcResult;

import com.fastfood.fast_food_backend.entities.Categoria;
import com.fastfood.fast_food_backend.entities.Inventario;
import com.fastfood.fast_food_backend.entities.Producto;
import com.fastfood.fast_food_backend.entities.Usuario;
import com.fastfood.fast_food_backend.repositories.CategoriaRepository;
import com.fastfood.fast_food_backend.repositories.InventarioRepository;
import com.fastfood.fast_food_backend.repositories.ProductoRepository;
import com.fastfood.fast_food_backend.repositories.UsuarioRepository;
import com.fastfood.fast_food_backend.services.ProductoService;
import com.jayway.jsonpath.JsonPath;

@SpringBootTest
@AutoConfigureMockMvc
class RegistroCatalogoIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private JdbcTemplate jdbc;
    @Autowired private UsuarioRepository usuarios;
    @Autowired private ProductoRepository productos;
    @Autowired private CategoriaRepository categorias;
    @Autowired private InventarioRepository inventarios;
    @Autowired private ProductoService productoService;
    @Autowired private PasswordEncoder passwordEncoder;

    private String tokenAdmin;
    private String tokenEmpleado;

    @BeforeEach
    void preparar() throws Exception {
        for (String tabla : List.of("pagos", "carrito_items", "detalles_pedido", "imagenes_producto",
                "inventarios", "pedidos", "productos", "usuarios")) {
            jdbc.update("DELETE FROM " + tabla);
        }
        jdbc.update("DELETE FROM categorias WHERE nombre LIKE 'PRUEBA_REGISTRO%'");
        tokenAdmin = login(usuario("admin.registro@fastfood.test", "ADMINISTRADOR"));
        tokenEmpleado = login(usuario("empleado.registro@fastfood.test", "EMPLEADO"));
    }

    @Test
    void crearProductoConIdExistenteNoSobrescribeElProducto() throws Exception {
        Producto original = productos.save(producto("Inca Kola 500ml", "4.00", 50));

        MvcResult resultado = mockMvc.perform(post("/api/productos").header("Authorization", bearer(tokenAdmin))
                        .contentType(MediaType.APPLICATION_JSON).content("""
                                {"id":%d,"nombre":"Hamburguesa doble","descripcion":"Doble carne",
                                 "precio":18,"stock":10,"categoria":"HAMBURGUESAS"}
                                """.formatted(original.getId())))
                .andExpect(status().isCreated()).andReturn();

        assertNotEquals(original.getId(), idRespuesta(resultado));
        Producto conservado = productos.findById(original.getId()).orElseThrow();
        assertEquals("Inca Kola 500ml", conservado.getNombre());
        assertEquals(0, new BigDecimal("4.00").compareTo(conservado.getPrecio()));
        assertEquals(50, conservado.getStock());
        assertEquals(2, productos.count());
    }

    @Test
    void crearCategoriaConIdExistenteNoSobrescribeLaCategoria() throws Exception {
        Categoria original = new Categoria();
        original.setNombre("PRUEBA_REGISTRO_ORIGINAL");
        original.setDescripcion("Descripción original");
        original.setActivo(true);
        original = categorias.save(original);

        MvcResult resultado = mockMvc.perform(post("/api/categorias").header("Authorization", bearer(tokenAdmin))
                        .contentType(MediaType.APPLICATION_JSON).content("""
                                {"id":%d,"nombre":"prueba_registro_nueva","descripcion":"Nueva","activo":false}
                                """.formatted(original.getId())))
                .andExpect(status().isCreated()).andReturn();

        assertNotEquals(original.getId(), idRespuesta(resultado));
        Categoria conservada = categorias.findById(original.getId()).orElseThrow();
        assertEquals("PRUEBA_REGISTRO_ORIGINAL", conservada.getNombre());
        assertEquals("Descripción original", conservada.getDescripcion());
        assertEquals(true, conservada.getActivo());
    }

    @Test
    void crearInventarioConIdExistenteNoCambiaElProductoDeOtraConfiguracion() throws Exception {
        Producto conInventario = productoService.registrar(producto("Papas fritas", "6.00", 20));
        Inventario original = inventarios.findByProductoId(conInventario.getId()).orElseThrow();
        Producto sinInventario = productos.save(producto("Gaseosa", "3.00", 30));

        // Crear configuraciones de inventario es del administrador; el empleado recibe 403.
        mockMvc.perform(post("/api/inventarios").header("Authorization", bearer(tokenEmpleado))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"producto\":{\"id\":%d},\"stockMinimo\":5}".formatted(sinInventario.getId())))
                .andExpect(status().isForbidden());

        MvcResult resultado = mockMvc.perform(post("/api/inventarios").header("Authorization", bearer(tokenAdmin))
                        .contentType(MediaType.APPLICATION_JSON).content("""
                                {"id":%d,"producto":{"id":%d},"stockMinimo":5,
                                 "fechaStockBajo":"2000-01-01T00:00:00","fechaAgotado":"2000-01-01T00:00:00"}
                                """.formatted(original.getId(), sinInventario.getId())))
                .andExpect(status().isCreated()).andReturn();

        Long idNuevo = idRespuesta(resultado);
        assertNotEquals(original.getId(), idNuevo);
        assertEquals(conInventario.getId(),
                inventarios.findById(original.getId()).orElseThrow().getProducto().getId());
        Inventario nuevo = inventarios.findById(idNuevo).orElseThrow();
        assertEquals(sinInventario.getId(), nuevo.getProducto().getId());
        // El stock 30 está sobre el mínimo 5: las fechas de alerta enviadas no se guardan.
        assertNull(nuevo.getFechaStockBajo());
        assertNull(nuevo.getFechaAgotado());
    }

    @Test
    void renombrarCategoriaActualizaElNombreEnSusProductos() throws Exception {
        Categoria piqueos = new Categoria();
        piqueos.setNombre("PRUEBA_REGISTRO_PIQUEOS");
        piqueos.setActivo(true);
        piqueos = categorias.save(piqueos);
        Producto papas = producto("Papas fritas", "6.00", 10);
        papas.setCategoria(piqueos.getNombre());
        papas = productoService.registrar(papas);
        Producto hamburguesa = productoService.registrar(producto("Hamburguesa", "12.00", 10));

        mockMvc.perform(put("/api/categorias/{id}", piqueos.getId()).header("Authorization", bearer(tokenAdmin))
                        .contentType(MediaType.APPLICATION_JSON).content("""
                                {"nombre":"prueba_registro_acompanamientos","descripcion":"Renombrada","activo":true}
                                """))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/productos/{id}", papas.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.categoria").value("PRUEBA_REGISTRO_ACOMPANAMIENTOS"))
                .andExpect(jsonPath("$.categoriaId").value(piqueos.getId().intValue()))
                .andExpect(jsonPath("$.nombre").value("Papas fritas"))
                .andExpect(jsonPath("$.stock").value(10));
        assertEquals("HAMBURGUESAS", productos.findById(hamburguesa.getId()).orElseThrow().getCategoria());
    }

    private Producto producto(String nombre, String precio, int stock) {
        Producto producto = new Producto();
        producto.setNombre(nombre);
        producto.setPrecio(new BigDecimal(precio));
        producto.setStock(stock);
        producto.setCategoria("HAMBURGUESAS");
        return producto;
    }

    private Usuario usuario(String correo, String rol) {
        Usuario usuario = new Usuario();
        usuario.setNombre("Usuario de prueba");
        usuario.setCorreo(correo);
        usuario.setContrasena(passwordEncoder.encode("123456"));
        usuario.setTelefono("999111222");
        usuario.setRol(rol);
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

    private Long idRespuesta(MvcResult resultado) throws Exception {
        Number id = JsonPath.read(resultado.getResponse().getContentAsString(), "$.id");
        return id.longValue();
    }

    private String bearer(String token) { return "Bearer " + token; }
}
