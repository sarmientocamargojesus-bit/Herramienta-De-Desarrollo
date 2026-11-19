package com.fastfood.fast_food_backend.services;

import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fastfood.fast_food_backend.dto.RegistroUsuarioRequest;
import com.fastfood.fast_food_backend.entities.Usuario;
import com.fastfood.fast_food_backend.exceptions.RecursoNoEncontradoException;
import com.fastfood.fast_food_backend.exceptions.ReglaNegocioException;
import com.fastfood.fast_food_backend.repositories.UsuarioRepository;

@Service
public class UsuarioService {

    private static final String ROL_PREDETERMINADO = "CLIENTE";
    private static final Set<String> ROLES_PERMITIDOS = Set.of("CLIENTE", "EMPLEADO", "ADMINISTRADOR");
    private static final String ROL_ADMINISTRADOR = "ADMINISTRADOR";
    private static final Pattern FORMATO_CORREO = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");
    private static final int MAX_NOMBRE = 100;
    private static final int MAX_CORREO = 150;
    private static final int MAX_TELEFONO = 20;
    private static final int MAX_DIRECCION = 255;
    private static final int MIN_CONTRASENA = 6;

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
        usuario.setCorreo(usuario.getCorreo().trim().toLowerCase(Locale.ROOT));
        usuario.setRol(ROL_PREDETERMINADO);
        usuario.setActivo(usuario.getActivo() == null ? true : usuario.getActivo());
        usuario.setContrasena(passwordEncoder.encode(usuario.getContrasena()));
        return usuarioRepository.save(usuario);
    }

    public Usuario actualizar(Long id, Usuario datos) {
        return actualizar(id, datos, null);
    }

    // correoActual es quien hace el cambio; un administrador no puede desactivarse a sí mismo.
    @Transactional
    public Usuario actualizar(Long id, Usuario datos, String correoActual) {
        Usuario usuario = buscarPorId(id);
        validar(datos, false);
        if (datos.getContrasena() != null && !datos.getContrasena().isBlank()
                && datos.getContrasena().length() < MIN_CONTRASENA) {
            throw new IllegalArgumentException("La contraseña debe tener al menos 6 caracteres");
        }
        if (Boolean.FALSE.equals(datos.getActivo()) && Boolean.TRUE.equals(usuario.getActivo())) {
            protegerAdministrador(usuario, correoActual, "desactivar");
        }
        validarCorreoDisponible(datos.getCorreo(), id);
        usuario.setNombre(datos.getNombre());
        usuario.setCorreo(datos.getCorreo().trim().toLowerCase(Locale.ROOT));
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
        return cambiarRol(id, nuevoRol, null);
    }

    @Transactional
    public Usuario cambiarRol(Long id, String nuevoRol, String correoActual) {
        if (nuevoRol == null || nuevoRol.isBlank()) {
            throw new IllegalArgumentException("El nuevo rol es obligatorio");
        }
        String rolNormalizado = nuevoRol.trim().toUpperCase(Locale.ROOT);
        if (!ROLES_PERMITIDOS.contains(rolNormalizado)) {
            throw new IllegalArgumentException("Rol no permitido: " + nuevoRol);
        }
        Usuario usuario = buscarPorId(id);
        if (!ROL_ADMINISTRADOR.equals(rolNormalizado)) {
            protegerAdministrador(usuario, correoActual, "quitar el rol de administrador a");
        }
        usuario.setRol(rolNormalizado);
        return usuarioRepository.save(usuario);
    }

    public void eliminar(Long id) {
        eliminar(id, null);
    }

    @Transactional
    public void eliminar(Long id, String correoActual) {
        Usuario usuario = buscarPorId(id);
        protegerAdministrador(usuario, correoActual, "eliminar");
        usuarioRepository.delete(usuario);
    }

    // Evita quedarse sin administradores: no se puede quitar, desactivar ni borrar al último administrador
    // activo, ni que un administrador se quite el acceso a sí mismo.
    private void protegerAdministrador(Usuario usuario, String correoActual, String accion) {
        if (!ROL_ADMINISTRADOR.equalsIgnoreCase(usuario.getRol())) {
            return;
        }
        if (correoActual != null && usuario.getCorreo().equalsIgnoreCase(correoActual)) {
            throw new ReglaNegocioException("No puedes " + accion + " tu propia cuenta de administrador");
        }
        if (Boolean.TRUE.equals(usuario.getActivo())
                && usuarioRepository.countByRolIgnoreCaseAndActivoTrue(ROL_ADMINISTRADOR) <= 1) {
            throw new ReglaNegocioException("Debe quedar al menos un administrador activo");
        }
    }

    private void validar(Usuario usuario, boolean exigirContrasena) {
        if (usuario == null || usuario.getNombre() == null || usuario.getNombre().isBlank()) {
            throw new IllegalArgumentException("El nombre del usuario es obligatorio");
        }
        if (usuario.getNombre().trim().length() > MAX_NOMBRE) {
            throw new IllegalArgumentException("El nombre no puede tener más de " + MAX_NOMBRE + " caracteres");
        }
        if (usuario.getCorreo() == null || usuario.getCorreo().isBlank()
                || usuario.getCorreo().trim().length() > MAX_CORREO
                || !FORMATO_CORREO.matcher(usuario.getCorreo().trim()).matches()) {
            throw new IllegalArgumentException("El correo del usuario no es válido");
        }
        if (usuario.getTelefono() == null || usuario.getTelefono().isBlank()) {
            throw new IllegalArgumentException("El teléfono del usuario es obligatorio");
        }
        if (usuario.getTelefono().trim().length() > MAX_TELEFONO) {
            throw new IllegalArgumentException("El teléfono no puede tener más de " + MAX_TELEFONO + " caracteres");
        }
        if (usuario.getDireccion() != null && usuario.getDireccion().length() > MAX_DIRECCION) {
            throw new IllegalArgumentException("La dirección no puede tener más de " + MAX_DIRECCION + " caracteres");
        }
        if (exigirContrasena && (usuario.getContrasena() == null || usuario.getContrasena().length() < MIN_CONTRASENA)) {
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
