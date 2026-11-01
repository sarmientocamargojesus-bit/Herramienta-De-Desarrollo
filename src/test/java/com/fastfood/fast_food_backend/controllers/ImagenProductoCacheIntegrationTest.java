package com.fastfood.fast_food_backend.controllers;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.fastfood.fast_food_backend.entities.Producto;
import com.fastfood.fast_food_backend.entities.Usuario;
import com.fastfood.fast_food_backend.repositories.ProductoRepository;
import com.fastfood.fast_food_backend.repositories.UsuarioRepository;
import com.jayway.jsonpath.JsonPath;

@SpringBootTest
@AutoConfigureMockMvc
class ImagenProductoCacheIntegrationTest {

    @Autowired private MockMvc mvc;
    @Autowired private JdbcTemplate jdbc;
    @Autowired private ProductoRepository productos;
    @Autowired private UsuarioRepository usuarios;
    @Autowired private PasswordEncoder encoder;

    private Long productoId;
    private String token;

    @BeforeEach
    void preparar() throws Exception {
        for (String tabla : List.of("pagos", "carrito_items", "detalles_pedido", "imagenes_producto",
                "inventarios", "pedidos", "productos", "usuarios")) {
            jdbc.update("DELETE FROM " + tabla);
        }
        Producto producto = new Producto();
        producto.setNombre("Producto con foto");
        producto.setPrecio(BigDecimal.TEN);
        producto.setStock(2);
        producto.setCategoria("HAMBURGUESAS");
        productoId = productos.save(producto).getId();
        token = iniciarSesionAdministrador();
    }

    @Test
    void elProductoIndicaSiTieneFoto() throws Exception {
        mvc.perform(get("/api/productos/{id}", productoId))
                .andExpect(jsonPath("$.tieneImagen").value(false));

        subir(new byte[] {(byte) 0x89, 'P', 'N', 'G', 13, 10, 26, 10, 1});

        mvc.perform(get("/api/productos/{id}", productoId))
                .andExpect(jsonPath("$.tieneImagen").value(true));
        mvc.perform(get("/api/productos"))
                .andExpect(jsonPath("$[0].tieneImagen").value(true));
    }

    @Test
    void laFotoSeGuardaEnCacheYSeValidaConEtag() throws Exception {
        subir(new byte[] {(byte) 0x89, 'P', 'N', 'G', 13, 10, 26, 10, 1});

        MvcResult primera = mvc.perform(get("/api/productos/{id}/imagen", productoId))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CACHE_CONTROL, "no-cache"))
                .andReturn();
        String etag = primera.getResponse().getHeader(HttpHeaders.ETAG);
        assertNotNull(etag);

        mvc.perform(get("/api/productos/{id}/imagen", productoId).header(HttpHeaders.IF_NONE_MATCH, etag))
                .andExpect(status().isNotModified());

        subir(new byte[] {(byte) 0x89, 'P', 'N', 'G', 13, 10, 26, 10, 2});
        mvc.perform(get("/api/productos/{id}/imagen", productoId).header(HttpHeaders.IF_NONE_MATCH, etag))
                .andExpect(status().isOk());
    }

    private void subir(byte[] datos) throws Exception {
        mvc.perform(multipart("/api/productos/{id}/imagen", productoId)
                        .file(new MockMultipartFile("archivo", "foto.png", "image/png", datos))
                        .header("Authorization", "Bearer " + token)
                        .with(solicitud -> { solicitud.setMethod("PUT"); return solicitud; }))
                .andExpect(status().isNoContent());
    }

    private String iniciarSesionAdministrador() throws Exception {
        Usuario usuario = new Usuario();
        usuario.setNombre("Administrador de imágenes");
        usuario.setCorreo("admin.cache@fastfood.test");
        usuario.setContrasena(encoder.encode("123456"));
        usuario.setTelefono("999111222");
        usuario.setRol("ADMINISTRADOR");
        usuario.setActivo(true);
        usuarios.save(usuario);
        MvcResult resultado = mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"correo\":\"admin.cache@fastfood.test\",\"contrasena\":\"123456\"}"))
                .andExpect(status().isOk()).andReturn();
        return JsonPath.read(resultado.getResponse().getContentAsString(), "$.token");
    }
}
