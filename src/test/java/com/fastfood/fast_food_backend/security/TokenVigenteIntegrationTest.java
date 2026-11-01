package com.fastfood.fast_food_backend.security;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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

import com.fastfood.fast_food_backend.entities.Usuario;
import com.fastfood.fast_food_backend.repositories.UsuarioRepository;
import com.jayway.jsonpath.JsonPath;

@SpringBootTest
@AutoConfigureMockMvc
class TokenVigenteIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private JdbcTemplate jdbc;
    @Autowired private UsuarioRepository usuarios;
    @Autowired private PasswordEncoder passwordEncoder;

    private Usuario empleado;
    private String tokenEmpleado;
    private String tokenAdmin;

    @BeforeEach
    void preparar() throws Exception {
        for (String tabla : List.of("pagos", "carrito_items", "detalles_pedido", "imagenes_producto",
                "inventarios", "pedidos", "productos", "usuarios")) {
            jdbc.update("DELETE FROM " + tabla);
        }
        empleado = usuario("empleado.token@fastfood.test", "EMPLEADO");
        usuario("admin.token@fastfood.test", "ADMINISTRADOR");
        tokenEmpleado = login("empleado.token@fastfood.test");
        tokenAdmin = login("admin.token@fastfood.test");
    }

    @Test
    void tokenDeUsuarioVigenteSigueFuncionando() throws Exception {
        mockMvc.perform(get("/api/inventarios").header("Authorization", bearer(tokenEmpleado)))
                .andExpect(status().isOk());
    }

    @Test
    void cambiarElRolInvalidaElTokenAnterior() throws Exception {
        mockMvc.perform(put("/api/usuarios/{id}/rol", empleado.getId()).header("Authorization", bearer(tokenAdmin))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"rol\":\"CLIENTE\"}"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/inventarios").header("Authorization", bearer(tokenEmpleado)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.mensaje").value("La sesión ya no es válida. Inicia sesión nuevamente"));

        String tokenNuevo = login("empleado.token@fastfood.test");
        mockMvc.perform(get("/api/inventarios").header("Authorization", bearer(tokenNuevo)))
                .andExpect(status().isForbidden());
    }

    @Test
    void desactivarAlUsuarioInvalidaSuToken() throws Exception {
        empleado.setActivo(false);
        usuarios.save(empleado);

        mockMvc.perform(get("/api/inventarios").header("Authorization", bearer(tokenEmpleado)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void eliminarAlUsuarioInvalidaSuToken() throws Exception {
        usuarios.delete(empleado);

        mockMvc.perform(get("/api/inventarios").header("Authorization", bearer(tokenEmpleado)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void tokenInvalidadoNoImpideConsultarRutasPublicasSinToken() throws Exception {
        empleado.setActivo(false);
        usuarios.save(empleado);

        mockMvc.perform(get("/api/productos")).andExpect(status().isOk());
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

    private String login(String correo) throws Exception {
        var resultado = mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"correo":"%s","contrasena":"123456"}
                                """.formatted(correo)))
                .andExpect(status().isOk()).andReturn();
        return JsonPath.read(resultado.getResponse().getContentAsString(), "$.token");
    }

    private String bearer(String token) { return "Bearer " + token; }
}
