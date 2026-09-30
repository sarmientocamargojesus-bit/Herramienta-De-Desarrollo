package com.fastfood.fast_food_backend.security;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import com.fastfood.fast_food_backend.entities.Pedido;
import com.fastfood.fast_food_backend.entities.Usuario;
import com.fastfood.fast_food_backend.repositories.PedidoRepository;
import com.fastfood.fast_food_backend.repositories.UsuarioRepository;
import com.jayway.jsonpath.JsonPath;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class SeguridadIntegrationTest {

    private static final String CLAVE = "claveSegura123";

    @Autowired private MockMvc mockMvc;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private PedidoRepository pedidoRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    private Usuario clientePropietario;
    private Usuario otroCliente;
    private Usuario administrador;
    private Pedido pedido;

    @BeforeEach
    void prepararDatos() {
        clientePropietario = guardarUsuario("Cliente Uno", "cliente1@fastfood.com", "CLIENTE");
        otroCliente = guardarUsuario("Cliente Dos", "cliente2@fastfood.com", "CLIENTE");
        administrador = guardarUsuario("Administrador", "admin.seguridad@fastfood.com", "ADMINISTRADOR");
        pedido = pedidoRepository.save(crearPedido(clientePropietario));
    }

    @Test
    void debePermitirConsultarProductosSinIniciarSesion() throws Exception {
        mockMvc.perform(get("/api/productos"))
                .andExpect(status().isOk());
    }

    @Test
    void debeResponder401CuandoFaltaAutenticacion() throws Exception {
        mockMvc.perform(get("/api/usuarios"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.codigo").value(401));
    }

    @Test
    void debeResponder403CuandoClienteIntentaAdministrarUsuarios() throws Exception {
        mockMvc.perform(get("/api/usuarios")
                        .header("Authorization", "Bearer " + iniciarSesion(clientePropietario.getCorreo())))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.codigo").value(403));
    }

    @Test
    void debePermitirAlAdministradorConsultarUsuarios() throws Exception {
        mockMvc.perform(get("/api/usuarios")
                        .header("Authorization", "Bearer " + iniciarSesion(administrador.getCorreo())))
                .andExpect(status().isOk());
    }

    @Test
    void debePermitirAlClienteConsultarSuPedido() throws Exception {
        mockMvc.perform(get("/api/pedidos/{id}", pedido.getId())
                        .header("Authorization", "Bearer " + iniciarSesion(clientePropietario.getCorreo())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(pedido.getId()));
    }

    @Test
    void debeImpedirAlClienteConsultarElPedidoDeOtroUsuario() throws Exception {
        mockMvc.perform(get("/api/pedidos/{id}", pedido.getId())
                        .header("Authorization", "Bearer " + iniciarSesion(otroCliente.getCorreo())))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.codigo").value(403));
    }

    private String iniciarSesion(String correo) throws Exception {
        MvcResult resultado = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "correo": "%s",
                                  "contrasena": "%s"
                                }
                                """.formatted(correo, CLAVE)))
                .andExpect(status().isOk())
                .andReturn();
        return JsonPath.read(resultado.getResponse().getContentAsString(), "$.token");
    }

    private Usuario guardarUsuario(String nombre, String correo, String rol) {
        Usuario usuario = new Usuario();
        usuario.setNombre(nombre);
        usuario.setCorreo(correo);
        usuario.setContrasena(passwordEncoder.encode(CLAVE));
        usuario.setTelefono("999111222");
        usuario.setDireccion("Ica");
        usuario.setRol(rol);
        usuario.setActivo(true);
        return usuarioRepository.save(usuario);
    }

    private Pedido crearPedido(Usuario usuario) {
        Pedido nuevoPedido = new Pedido();
        nuevoPedido.setUsuario(usuario);
        nuevoPedido.setTelefono(usuario.getTelefono());
        nuevoPedido.setFecha(LocalDateTime.now());
        nuevoPedido.setFechaActualizacion(LocalDateTime.now());
        nuevoPedido.setEstado("PENDIENTE");
        nuevoPedido.setSubtotal(new BigDecimal("20.00"));
        nuevoPedido.setCostoEntrega(BigDecimal.ZERO);
        nuevoPedido.setTotal(new BigDecimal("20.00"));
        nuevoPedido.setTipoEntrega("RECOJO");
        return nuevoPedido;
    }
}
