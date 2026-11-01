package com.fastfood.fast_food_backend.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import com.fastfood.fast_food_backend.entities.Categoria;
import com.fastfood.fast_food_backend.entities.Usuario;
import com.fastfood.fast_food_backend.repositories.CategoriaRepository;
import com.fastfood.fast_food_backend.repositories.UsuarioRepository;
import com.fastfood.fast_food_backend.services.InventarioService;

@ExtendWith(MockitoExtension.class)
class InicializadorDatosTest {

    @Mock private CategoriaRepository categoriaRepository;
    @Mock private UsuarioRepository usuarioRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private InventarioService inventarioService;

    @Test
    void completaLosInventariosFaltantesConMinimoCinco() {
        when(categoriaRepository.findByNombreIgnoreCase(anyString())).thenReturn(Optional.of(new Categoria()));

        InicializadorDatos inicializador = new InicializadorDatos(
                categoriaRepository, usuarioRepository, passwordEncoder, inventarioService);
        inicializador.run();

        verify(inventarioService).completarFaltantes(5);
    }

    @Test
    void conservaDatosDelUsuarioExistenteAlPromoverlo() {
        Usuario usuario = new Usuario();
        usuario.setId(3L);
        usuario.setNombre("Nombre original");
        usuario.setCorreo("cliente@example.com");
        usuario.setTelefono("987654321");
        usuario.setDireccion("Ica, Perú");
        usuario.setActivo(false);
        usuario.setContrasena("hash-anterior");
        usuario.setRol("CLIENTE");

        when(categoriaRepository.findByNombreIgnoreCase(anyString())).thenReturn(Optional.of(new Categoria()));
        when(usuarioRepository.findByCorreoIgnoreCase("cliente@example.com")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.encode("clave-elegida")).thenReturn("hash-nuevo");

        InicializadorDatos inicializador = nuevoInicializador("CLIENTE@example.com");
        inicializador.run();

        verify(usuarioRepository).save(usuario);
        assertEquals(3L, usuario.getId());
        assertEquals("Nombre original", usuario.getNombre());
        assertEquals("cliente@example.com", usuario.getCorreo());
        assertEquals("987654321", usuario.getTelefono());
        assertEquals("Ica, Perú", usuario.getDireccion());
        assertFalse(usuario.getActivo());
        assertEquals("ADMINISTRADOR", usuario.getRol());
        assertEquals("hash-nuevo", usuario.getContrasena());
    }

    @Test
    void creaAdministradorInicialCuandoNoExisteUsuario() {
        when(categoriaRepository.findByNombreIgnoreCase(anyString())).thenReturn(Optional.of(new Categoria()));
        when(usuarioRepository.findByCorreoIgnoreCase("nuevo@example.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("clave-elegida")).thenReturn("hash-nuevo");

        InicializadorDatos inicializador = nuevoInicializador("NUEVO@example.com");
        inicializador.run();

        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).save(captor.capture());
        Usuario administrador = captor.getValue();
        assertEquals("Administrador", administrador.getNombre());
        assertEquals("nuevo@example.com", administrador.getCorreo());
        assertEquals("000000000", administrador.getTelefono());
        assertEquals("ADMINISTRADOR", administrador.getRol());
        assertEquals("hash-nuevo", administrador.getContrasena());
        assertTrue(administrador.getActivo());
    }

    private InicializadorDatos nuevoInicializador(String correo) {
        InicializadorDatos inicializador = new InicializadorDatos(
                categoriaRepository, usuarioRepository, passwordEncoder, inventarioService);
        ReflectionTestUtils.setField(inicializador, "correoAdministrador", correo);
        ReflectionTestUtils.setField(inicializador, "contrasenaAdministrador", "clave-elegida");
        return inicializador;
    }
}
