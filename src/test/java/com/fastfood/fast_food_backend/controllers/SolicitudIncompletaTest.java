package com.fastfood.fast_food_backend.controllers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import com.fastfood.fast_food_backend.entities.Usuario;
import com.fastfood.fast_food_backend.repositories.UsuarioRepository;
import com.jayway.jsonpath.JsonPath;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class SolicitudIncompletaTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private UsuarioRepository usuarios;
    @Autowired private PasswordEncoder encoder;

    @Test
    void busquedaSinNombreRespondeSolicitudIncorrecta() throws Exception {
        mockMvc.perform(get("/api/productos/buscar"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value(400))
                .andExpect(jsonPath("$.mensaje").value("Falta el parámetro obligatorio 'nombre'"));
    }

    @Test
    void rangoSinMaximoRespondeSolicitudIncorrecta() throws Exception {
        mockMvc.perform(get("/api/productos/rango-precios").param("minimo", "1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("Falta el parámetro obligatorio 'maximo'"));
    }

    @Test
    void imagenSinArchivoRespondeSolicitudIncorrecta() throws Exception {
        mockMvc.perform(multipart("/api/productos/1/imagen").header("Authorization", bearerAdministrador())
                        .with(solicitud -> { solicitud.setMethod("PUT"); return solicitud; }))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("Falta el archivo de imagen en el campo 'archivo'"));
    }

    @Test
    void imagenEnviadaComoJsonRespondeTipoNoSoportado() throws Exception {
        mockMvc.perform(put("/api/productos/1/imagen").header("Authorization", bearerAdministrador())
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isUnsupportedMediaType())
                .andExpect(jsonPath("$.codigo").value(415));
    }

    @Test
    void metodoNoPermitidoRespondeMetodoNoPermitido() throws Exception {
        mockMvc.perform(patch("/api/productos/1").header("Authorization", bearerAdministrador()))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.codigo").value(405));
    }

    private String bearerAdministrador() throws Exception {
        Usuario usuario = new Usuario();
        usuario.setNombre("Administrador de solicitudes");
        usuario.setCorreo("admin.solicitudes@fastfood.test");
        usuario.setContrasena(encoder.encode("123456"));
        usuario.setTelefono("999111222");
        usuario.setRol("ADMINISTRADOR");
        usuario.setActivo(true);
        usuarios.save(usuario);
        MvcResult resultado = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"correo\":\"admin.solicitudes@fastfood.test\",\"contrasena\":\"123456\"}"))
                .andExpect(status().isOk()).andReturn();
        return "Bearer " + JsonPath.read(resultado.getResponse().getContentAsString(), "$.token");
    }
}
