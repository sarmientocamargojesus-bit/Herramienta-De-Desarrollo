package com.fastfood.fast_food_backend.controllers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import com.fastfood.fast_food_backend.entities.Categoria;
import com.fastfood.fast_food_backend.entities.Producto;
import com.fastfood.fast_food_backend.entities.Usuario;
import com.fastfood.fast_food_backend.repositories.CategoriaRepository;
import com.fastfood.fast_food_backend.repositories.ProductoRepository;
import com.fastfood.fast_food_backend.repositories.UsuarioRepository;
import com.jayway.jsonpath.JsonPath;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ImagenProductoControllerTest {

    @Autowired private MockMvc mvc;
    @Autowired private CategoriaRepository categorias;
    @Autowired private ProductoRepository productos;
    @Autowired private UsuarioRepository usuarios;
    @Autowired private PasswordEncoder encoder;

    private Long productoId;

    @BeforeEach
    void preparar() {
        Categoria categoria = new Categoria();
        categoria.setNombre("IMAGENES_API");
        categoria.setActivo(true);
        categoria = categorias.save(categoria);
        Producto producto = new Producto();
        producto.setNombre("Producto para imagen");
        producto.setPrecio(BigDecimal.TEN);
        producto.setStock(2);
        producto.setCategoria(categoria.getNombre());
        producto.setCategoriaReferencia(categoria);
        productoId = productos.save(producto).getId();
    }

    @Test
    void exigeAdministradorParaSubir() throws Exception {
        mvc.perform(multipart("/api/productos/{id}/imagen", productoId).file(archivo())
                        .with(solicitud -> { solicitud.setMethod("PUT"); return solicitud; }))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void clienteNoPuedeSubir() throws Exception {
        String token = iniciarSesion("cliente.imagenes@fastfood.com", "CLIENTE");
        mvc.perform(multipart("/api/productos/{id}/imagen", productoId).file(archivo())
                        .header("Authorization", "Bearer " + token)
                        .with(solicitud -> { solicitud.setMethod("PUT"); return solicitud; }))
                .andExpect(status().isForbidden());
    }

    @Test
    void administradorSubeYQuitaImagenPublica() throws Exception {
        String token = iniciarSesion("admin.imagenes@fastfood.com", "ADMINISTRADOR");
        String ruta = "/api/productos/" + productoId + "/imagen";
        mvc.perform(multipart(ruta).file(archivo()).header("Authorization", "Bearer " + token)
                        .with(solicitud -> { solicitud.setMethod("PUT"); return solicitud; }))
                .andExpect(status().isNoContent());
        mvc.perform(get(ruta))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.IMAGE_PNG));
        mvc.perform(delete(ruta).header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());
        mvc.perform(get(ruta)).andExpect(status().isNotFound());
    }

    private MockMultipartFile archivo() {
        return new MockMultipartFile("archivo", "foto.png", "image/png",
                new byte[] {(byte) 0x89, 'P', 'N', 'G', 13, 10, 26, 10, 1});
    }

    private String iniciarSesion(String correo, String rol) throws Exception {
        Usuario usuario = new Usuario();
        usuario.setNombre("Usuario de imagenes");
        usuario.setCorreo(correo);
        usuario.setContrasena(encoder.encode("123456"));
        usuario.setTelefono("999111222");
        usuario.setRol(rol);
        usuario.setActivo(true);
        usuarios.save(usuario);
        MvcResult resultado = mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"correo\":\"" + correo + "\",\"contrasena\":\"123456\"}"))
                .andExpect(status().isOk()).andReturn();
        return JsonPath.read(resultado.getResponse().getContentAsString(), "$.token");
    }
}
