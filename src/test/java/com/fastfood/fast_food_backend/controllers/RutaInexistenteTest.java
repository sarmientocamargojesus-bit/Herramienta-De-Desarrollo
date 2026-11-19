package com.fastfood.fast_food_backend.controllers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import com.fastfood.fast_food_backend.entities.Usuario;
import com.fastfood.fast_food_backend.repositories.UsuarioRepository;
import com.jayway.jsonpath.JsonPath;

@SpringBootTest
@AutoConfigureMockMvc
class RutaInexistenteTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UsuarioRepository usuarios;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // Sin sesión, una ruta /api desconocida pide autenticación y no revela si existe.
    @Test
    void debePedirSesionParaRutaApiInexistenteSinToken() throws Exception {
        mockMvc.perform(get("/api/ruta-inexistente"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void debeResponderNoEncontradoParaRutaInexistenteConSesion() throws Exception {
        String correo = "ruta.inexistente@fastfood.test";
        if (usuarios.findByCorreoIgnoreCase(correo).isEmpty()) {
            Usuario usuario = new Usuario();
            usuario.setNombre("Cliente de prueba");
            usuario.setCorreo(correo);
            usuario.setContrasena(passwordEncoder.encode("123456"));
            usuario.setTelefono("999111222");
            usuario.setRol("CLIENTE");
            usuario.setActivo(true);
            usuarios.save(usuario);
        }
        var login = mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"correo\":\"" + correo + "\",\"contrasena\":\"123456\"}"))
                .andExpect(status().isOk()).andReturn();
        String token = JsonPath.read(login.getResponse().getContentAsString(), "$.token");

        mockMvc.perform(get("/api/ruta-inexistente").header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.codigo").value(404))
                .andExpect(jsonPath("$.ruta").value("/api/ruta-inexistente"));
    }
}
