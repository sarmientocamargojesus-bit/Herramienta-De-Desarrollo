package com.fastfood.fast_food_backend.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.fastfood.fast_food_backend.entities.Usuario;
import com.fastfood.fast_food_backend.repositories.UsuarioRepository;
import com.jayway.jsonpath.JsonPath;

import jakarta.persistence.EntityManager;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class RegistroUsuarioIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private UsuarioRepository usuarios;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private EntityManager entityManager;

    @ParameterizedTest
    @ValueSource(strings = {"CLIENTE", "ADMINISTRADOR"})
    void registroConIdExistenteCreaOtraCuentaYConservaLaOriginal(String rolOriginal) throws Exception {
        Usuario original = guardarUsuario(rolOriginal);
        Long idOriginal = original.getId();
        String correoOriginal = original.getCorreo();
        String hashOriginal = original.getContrasena();
        Boolean activoOriginal = original.getActivo();
        long cantidadAntes = usuarios.count();

        var resultado = mockMvc.perform(post("/api/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "id": %d,
                                  "nombre": "Cuenta nueva",
                                  "correo": "nueva.%s@fastfood.test",
                                  "contrasena": "654321",
                                  "telefono": "999222333",
                                  "direccion": "Direccion nueva",
                                  "rol": "ADMINISTRADOR",
                                  "activo": true
                                }
                                """.formatted(idOriginal, rolOriginal.toLowerCase())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.rol").value("CLIENTE"))
                .andExpect(jsonPath("$.contrasena").doesNotExist())
                .andReturn();

        Number idRespuesta = JsonPath.read(resultado.getResponse().getContentAsString(), "$.id");
        assertNotEquals(idOriginal.longValue(), idRespuesta.longValue());
        entityManager.flush();
        entityManager.clear();

        Usuario conservado = usuarios.findById(idOriginal).orElseThrow();
        assertEquals("Cuenta original", conservado.getNombre());
        assertEquals(correoOriginal, conservado.getCorreo());
        assertEquals(hashOriginal, conservado.getContrasena());
        assertEquals("999111222", conservado.getTelefono());
        assertEquals("Direccion original", conservado.getDireccion());
        assertEquals(rolOriginal, conservado.getRol());
        assertEquals(activoOriginal, conservado.getActivo());
        assertEquals(cantidadAntes + 1, usuarios.count());

        Usuario nueva = usuarios.findById(idRespuesta.longValue()).orElseThrow();
        assertEquals("Cuenta nueva", nueva.getNombre());
        assertEquals("999222333", nueva.getTelefono());
        assertEquals("Direccion nueva", nueva.getDireccion());
        assertEquals("CLIENTE", nueva.getRol());
        assertTrue(nueva.getActivo());
        assertTrue(passwordEncoder.matches("654321", nueva.getContrasena()));
    }

    @Test
    void registroNormalMantieneElContratoDelFrontend() throws Exception {
        long cantidadAntes = usuarios.count();
        mockMvc.perform(post("/api/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nombre": "Cliente nuevo",
                                  "correo": "NUEVO@fastfood.test",
                                  "contrasena": "123456",
                                  "telefono": "999333444",
                                  "direccion": "Ica"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.correo").value("nuevo@fastfood.test"))
                .andExpect(jsonPath("$.rol").value("CLIENTE"))
                .andExpect(jsonPath("$.activo").value(true))
                .andExpect(jsonPath("$.contrasena").doesNotExist());

        entityManager.flush();
        entityManager.clear();
        assertEquals(cantidadAntes + 1, usuarios.count());
        Usuario creado = usuarios.findByCorreoIgnoreCase("nuevo@fastfood.test").orElseThrow();
        assertTrue(passwordEncoder.matches("123456", creado.getContrasena()));
    }

    @Test
    void correoDuplicadoNoModificaLaCuentaNiCreaOtra() throws Exception {
        Usuario original = guardarUsuario("ADMINISTRADOR");
        Long idOriginal = original.getId();
        String correoOriginal = original.getCorreo();
        String hashOriginal = original.getContrasena();
        long cantidadAntes = usuarios.count();

        mockMvc.perform(post("/api/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "id": %d,
                                  "nombre": "Nombre que no debe guardarse",
                                  "correo": "%s",
                                  "contrasena": "654321",
                                  "telefono": "999888777"
                                }
                                """.formatted(idOriginal, correoOriginal)))
                .andExpect(status().isConflict());

        entityManager.flush();
        entityManager.clear();
        Usuario conservado = usuarios.findById(idOriginal).orElseThrow();
        assertEquals("Cuenta original", conservado.getNombre());
        assertEquals(hashOriginal, conservado.getContrasena());
        assertEquals("ADMINISTRADOR", conservado.getRol());
        assertEquals(cantidadAntes, usuarios.count());
    }

    private Usuario guardarUsuario(String rol) {
        Usuario usuario = new Usuario();
        usuario.setNombre("Cuenta original");
        usuario.setCorreo("original." + rol.toLowerCase() + "@fastfood.test");
        usuario.setContrasena(passwordEncoder.encode("123456"));
        usuario.setTelefono("999111222");
        usuario.setDireccion("Direccion original");
        usuario.setRol(rol);
        usuario.setActivo("ADMINISTRADOR".equals(rol));
        return usuarios.save(usuario);
    }
}
