package com.fastfood.fast_food_backend.security;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.fastfood.fast_food_backend.entities.Usuario;
import com.fastfood.fast_food_backend.repositories.UsuarioRepository;
import com.jayway.jsonpath.JsonPath;

@SpringBootTest
@AutoConfigureMockMvc
class JwtIntegrationTest {

    private static final String ADMIN_EMAIL = "jwt.admin@fastfood.test";
    private static final String CLIENTE_EMAIL = "jwt.cliente@fastfood.test";
    private static final String CONTRASENA = "ClaveSegura123";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    @BeforeEach
    void prepararUsuarios() {
        guardarUsuario(ADMIN_EMAIL, "ADMINISTRADOR");
        guardarUsuario(CLIENTE_EMAIL, "CLIENTE");
    }

    @Test
    void loginCorrectoDevuelveTokenJwt() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonLogin(ADMIN_EMAIL, CONTRASENA)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.tipo").value("Bearer"))
                .andExpect(jsonPath("$.correo").value(ADMIN_EMAIL))
                .andExpect(jsonPath("$.rol").value("ADMINISTRADOR"))
                .andExpect(jsonPath("$.expiraEnSegundos").value(3600));
    }

    @Test
    void credencialesIncorrectasDevuelven401() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonLogin(ADMIN_EMAIL, "clave-incorrecta")))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.codigo").value(401));
    }

    @Test
    void endpointProtegidoAceptaTokenValido() throws Exception {
        String token = iniciarSesion(ADMIN_EMAIL);

        mockMvc.perform(get("/api/usuarios")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    void endpointProtegidoSinTokenDevuelve401() throws Exception {
        mockMvc.perform(get("/api/usuarios"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.codigo").value(401));
    }

    @Test
    void tokenAlteradoDevuelve401() throws Exception {
        String token = iniciarSesion(ADMIN_EMAIL);
        char ultimo = token.charAt(token.length() - 1);
        String tokenAlterado = token.substring(0, token.length() - 1) + (ultimo == 'a' ? 'b' : 'a');

        mockMvc.perform(get("/api/usuarios")
                        .header("Authorization", "Bearer " + tokenAlterado))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.codigo").value(401));
    }

    @Test
    void tokenVencidoDevuelve401() throws Exception {
        Instant ahora = Instant.now();
        String tokenVencido = jwtService.generarToken(
                ADMIN_EMAIL,
                "ADMINISTRADOR",
                ahora.minusSeconds(7200),
                ahora.minusSeconds(3600));

        mockMvc.perform(get("/api/usuarios")
                        .header("Authorization", "Bearer " + tokenVencido))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.codigo").value(401));
    }

    @Test
    void clienteConTokenValidoNoPuedeAdministrarUsuarios() throws Exception {
        String token = iniciarSesion(CLIENTE_EMAIL);

        mockMvc.perform(get("/api/usuarios")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.codigo").value(403));
    }

    private String iniciarSesion(String correo) throws Exception {
        MvcResult resultado = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonLogin(correo, CONTRASENA)))
                .andExpect(status().isOk())
                .andReturn();
        return JsonPath.read(resultado.getResponse().getContentAsString(), "$.token");
    }

    private void guardarUsuario(String correo, String rol) {
        Usuario usuario = usuarioRepository.findByCorreoIgnoreCase(correo).orElseGet(Usuario::new);
        usuario.setNombre("Usuario JWT");
        usuario.setCorreo(correo);
        usuario.setContrasena(passwordEncoder.encode(CONTRASENA));
        usuario.setTelefono("999999999");
        usuario.setDireccion("Ica");
        usuario.setRol(rol);
        usuario.setActivo(true);
        usuarioRepository.save(usuario);
    }

    private String jsonLogin(String correo, String contrasena) {
        return """
                {
                  "correo": "%s",
                  "contrasena": "%s"
                }
                """.formatted(correo, contrasena);
    }
}
