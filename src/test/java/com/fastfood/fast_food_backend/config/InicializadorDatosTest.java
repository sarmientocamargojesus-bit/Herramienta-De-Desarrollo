package com.fastfood.fast_food_backend.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
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

@ExtendWith(MockitoExtension.class)
class InicializadorDatosTest {

    @Mock private CategoriaRepository categoriaRepository;
    @Mock private UsuarioRepository usuarioRepository;
    @Mock private PasswordEncoder passwordEncoder;

    @Test
    void noCambiaContrasenaNiRolDeUnaCuentaExistenteSiYaHayAdministrador() {
        Usuario usuario = usuarioExistente();
        when(categoriaRepository.findByNombreIgnoreCase(anyString())).thenReturn(Optional.of(new Categoria()));
        when(usuarioRepository.findByCorreoIgnoreCase("cliente@example.com")).thenReturn(Optional.of(usuario));
        when(usuarioRepository.countByRolIgnoreCaseAndActivoTrue("ADMINISTRADOR")).thenReturn(1L);

        nuevoInicializador("CLIENTE@example.com").run();

        verify(usuarioRepository, never()).save(any(Usuario.class));
        verify(passwordEncoder, never()).encode(anyString());
        assertEquals("hash-anterior", usuario.getContrasena());
        assertEquals("CLIENTE", usuario.getRol());
    }

    @Test
    void recuperaElAccesoSinTocarLaContrasenaCuandoNoQuedaAdministradorActivo() {
        Usuario usuario = usuarioExistente();
        when(categoriaRepository.findByNombreIgnoreCase(anyString())).thenReturn(Optional.of(new Categoria()));
        when(usuarioRepository.findByCorreoIgnoreCase("cliente@example.com")).thenReturn(Optional.of(usuario));
        when(usuarioRepository.countByRolIgnoreCaseAndActivoTrue("ADMINISTRADOR")).thenReturn(0L);

        nuevoInicializador("CLIENTE@example.com").run();

        verify(usuarioRepository).save(usuario);
        verify(passwordEncoder, never()).encode(anyString());
        assertEquals(3L, usuario.getId());
        assertEquals("Nombre original", usuario.getNombre());
        assertEquals("987654321", usuario.getTelefono());
        assertEquals("Ica, Perú", usuario.getDireccion());
        assertEquals("hash-anterior", usuario.getContrasena());
        assertEquals("ADMINISTRADOR", usuario.getRol());
        assertTrue(usuario.getActivo());
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

    private Usuario usuarioExistente() {
        Usuario usuario = new Usuario();
        usuario.setId(3L);
        usuario.setNombre("Nombre original");
        usuario.setCorreo("cliente@example.com");
        usuario.setTelefono("987654321");
        usuario.setDireccion("Ica, Perú");
        usuario.setActivo(false);
        usuario.setContrasena("hash-anterior");
        usuario.setRol("CLIENTE");
        return usuario;
    }

    private InicializadorDatos nuevoInicializador(String correo) {
        InicializadorDatos inicializador = new InicializadorDatos(
                categoriaRepository, usuarioRepository, passwordEncoder);
        ReflectionTestUtils.setField(inicializador, "correoAdministrador", correo);
        ReflectionTestUtils.setField(inicializador, "contrasenaAdministrador", "clave-elegida");
        return inicializador;
    }
}
