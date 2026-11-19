package com.fastfood.fast_food_backend.controllers;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fastfood.fast_food_backend.entities.Usuario;
import com.fastfood.fast_food_backend.dto.CambiarRolRequest;
import com.fastfood.fast_food_backend.dto.RegistroUsuarioRequest;
import com.fastfood.fast_food_backend.services.UsuarioService;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping
    public List<Usuario> listar() {
        return usuarioService.listar();
    }

    @GetMapping("/{id}")
    public Usuario buscarPorId(@PathVariable Long id) {
        return usuarioService.buscarPorId(id);
    }

    @GetMapping("/correo")
    public Usuario buscarPorCorreo(@RequestParam String correo) {
        return usuarioService.buscarPorCorreo(correo);
    }

    @GetMapping("/rol/{rol}")
    public List<Usuario> buscarPorRol(@PathVariable String rol) {
        return usuarioService.buscarPorRol(rol);
    }

    @PostMapping
    public ResponseEntity<Usuario> registrar(@RequestBody RegistroUsuarioRequest solicitud) {
        Usuario registrado = usuarioService.registrar(solicitud);
        return ResponseEntity.created(URI.create("/api/usuarios/" + registrado.getId())).body(registrado);
    }

    @PutMapping("/{id}")
    public Usuario actualizar(@PathVariable Long id, @RequestBody Usuario usuario, Authentication autenticacion) {
        return usuarioService.actualizar(id, usuario, autenticacion.getName());
    }

    @PutMapping("/{id}/rol")
    public Usuario cambiarRol(@PathVariable Long id, @RequestBody CambiarRolRequest solicitud,
            Authentication autenticacion) {
        if (solicitud == null) {
            throw new IllegalArgumentException("El nuevo rol es obligatorio");
        }
        return usuarioService.cambiarRol(id, solicitud.getRol(), autenticacion.getName());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id, Authentication autenticacion) {
        usuarioService.eliminar(id, autenticacion.getName());
        return ResponseEntity.noContent().build();
    }
}
