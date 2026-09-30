package com.fastfood.fast_food_backend.config;

import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.fastfood.fast_food_backend.entities.Categoria;
import com.fastfood.fast_food_backend.entities.Usuario;
import com.fastfood.fast_food_backend.repositories.CategoriaRepository;
import com.fastfood.fast_food_backend.repositories.UsuarioRepository;

@Component
public class InicializadorDatos implements CommandLineRunner {

    private final CategoriaRepository categoriaRepository;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${APP_ADMIN_EMAIL:}")
    private String correoAdministrador;

    @Value("${APP_ADMIN_PASSWORD:}")
    private String contrasenaAdministrador;

    public InicializadorDatos(
            CategoriaRepository categoriaRepository,
            UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder) {
        this.categoriaRepository = categoriaRepository;
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        crearSiNoExiste("HAMBURGUESAS", "Hamburguesas del menú");
        crearSiNoExiste("COMBOS", "Combos de productos");
        crearSiNoExiste("PIQUEOS", "Piqueos y acompañamientos");
        crearSiNoExiste("BEBIDAS", "Bebidas del menú");
        crearAdministradorInicial();
    }

    private void crearSiNoExiste(String nombre, String descripcion) {
        if (categoriaRepository.findByNombreIgnoreCase(nombre).isEmpty()) {
            Categoria categoria = new Categoria();
            categoria.setNombre(nombre);
            categoria.setDescripcion(descripcion);
            categoria.setActivo(true);
            categoriaRepository.save(categoria);
        }
    }

    private void crearAdministradorInicial() {
        if (correoAdministrador == null || correoAdministrador.isBlank()
                || contrasenaAdministrador == null || contrasenaAdministrador.length() < 6) {
            return;
        }

        String correo = correoAdministrador.trim().toLowerCase();
        Usuario administrador = usuarioRepository.findByCorreoIgnoreCase(correo).orElse(null);
        if (administrador == null) {
            administrador = new Usuario();
            administrador.setNombre("Administrador");
            administrador.setCorreo(correo);
            administrador.setTelefono("000000000");
            administrador.setActivo(true);
        }
        administrador.setContrasena(passwordEncoder.encode(contrasenaAdministrador));
        administrador.setRol("ADMINISTRADOR");
        usuarioRepository.save(administrador);
    }
}
