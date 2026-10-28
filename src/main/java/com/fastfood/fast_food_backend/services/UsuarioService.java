package com.fastfood.fast_food_backend.services;

import java.util.List;
import java.util.Set;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.fastfood.fast_food_backend.dto.RegistroUsuarioRequest;
import com.fastfood.fast_food_backend.entities.Usuario;
import com.fastfood.fast_food_backend.exceptions.RecursoNoEncontradoException;
import com.fastfood.fast_food_backend.exceptions.ReglaNegocioException;
import com.fastfood.fast_food_backend.repositories.UsuarioRepository;

@Service
public class UsuarioService {

    private static final String ROL_PREDETERMINADO = "CLIENTE";
    private static final Set<String> ROLES_PERMITIDOS = Set.of("CLIENTE", "EMPLEADO", "ADMINISTRADOR");

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<Usuario> listar() {
        return usuarioRepository.findAll();
    }

    public Usuario buscarPorId(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado: " + id));
    }

    public Usuario buscarPorCorreo(String correo) {
        return usuarioRepository.findByCorreoIgnoreCase(correo)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado con correo: " + correo));
    }

    public List<Usuario> buscarPorRol(String rol) {
        if (rol == null || rol.isBlank()) {
            throw new IllegalArgumentException("El rol es obligatorio");
        }
        return usuarioRepository.findByRolIgnoreCase(rol);
    }

    public Usuario registrar(RegistroUsuarioRequest solicitud) {
        if (solicitud == null) {
            throw new IllegalArgumentException("Los datos del usuario son obligatorios");
        }
        Usuario usuario = new Usuario();
        usuario.setNombre(solicitud.nombre());
        usuario.setCorreo(solicitud.correo());
        usuario.setContrasena(solicitud.contrasena());
        usuario.setTelefono(solicitud.telefono());
        usuario.setDireccion(solicitud.direccion());
        usuario.setActivo(solicitud.activo());
        validar(usuario, true);
        validarCorreoDisponible(usuario.getCorreo(), null);
        usuario.setCorreo(usuario.getCorreo().trim().toLowerCase());
        usuario.setRol(ROL_PREDETERMINADO);
        usuario.setActivo(usuario.getActivo() == null ? true : usuario.getActivo());
        usuario.setContrasena(passwordEncoder.encode(usuario.getContrasena()));
        return usuarioRepository.save(usuario);
    }

    public Usuario actualizar(Long id, Usuario datos) {
        Usuario usuario = buscarPorId(id);
        validar(datos, false);
        validarCorreoDisponible(datos.getCorreo(), id);
        usuario.setNombre(datos.getNombre());
        usuario.setCorreo(datos.getCorreo().trim().toLowerCase());
        usuario.setTelefono(datos.getTelefono());
        usuario.setDireccion(datos.getDireccion());
        if (datos.getActivo() != null) {
            usuario.setActivo(datos.getActivo());
        }
        if (datos.getContrasena() != null && !datos.getContrasena().isBlank()) {
            usuario.setContrasena(passwordEncoder.encode(datos.getContrasena()));
        }
        return usuarioRepository.save(usuario);
    }

    public Usuario cambiarRol(Long id, String nuevoRol) {
        if (nuevoRol == null || nuevoRol.isBlank()) {
            throw new IllegalArgumentException("El nuevo rol es obligatorio");
        }
        String rolNormalizado = nuevoRol.trim().toUpperCase();
        if (!ROLES_PERMITIDOS.contains(rolNormalizado)) {
            throw new IllegalArgumentException("Rol no permitido: " + nuevoRol);
        }
        Usuario usuario = buscarPorId(id);
        usuario.setRol(rolNormalizado);
        return usuarioRepository.save(usuario);
    }

    public void eliminar(Long id) {
        usuarioRepository.delete(buscarPorId(id));
    }

    private void validar(Usuario usuario, boolean exigirContrasena) {
        if (usuario == null || usuario.getNombre() == null || usuario.getNombre().isBlank()) {
            throw new IllegalArgumentException("El nombre del usuario es obligatorio");
        }
        if (usuario.getCorreo() == null || usuario.getCorreo().isBlank() || !usuario.getCorreo().contains("@")) {
            throw new IllegalArgumentException("El correo del usuario no es válido");
        }
        if (usuario.getTelefono() == null || usuario.getTelefono().isBlank()) {
            throw new IllegalArgumentException("El teléfono del usuario es obligatorio");
        }
        if (exigirContrasena && (usuario.getContrasena() == null || usuario.getContrasena().length() < 6)) {
            throw new IllegalArgumentException("La contraseña debe tener al menos 6 caracteres");
        }
    }

    private void validarCorreoDisponible(String correo, Long idActual) {
        usuarioRepository.findByCorreoIgnoreCase(correo).ifPresent(existente -> {
            if (idActual == null || !existente.getId().equals(idActual)) {
                throw new ReglaNegocioException("Ya existe un usuario con el correo: " + correo);
            }
        });
    }
}
