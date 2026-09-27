package com.fastfood.fast_food_backend.config;

import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.fastfood.fast_food_backend.entities.Categoria;
import com.fastfood.fast_food_backend.repositories.CategoriaRepository;

@Component
public class InicializadorDatos implements CommandLineRunner {

    private final CategoriaRepository categoriaRepository;

    public InicializadorDatos(CategoriaRepository categoriaRepository) {
        this.categoriaRepository = categoriaRepository;
    }

    @Override
    public void run(String... args) {
        crearSiNoExiste("HAMBURGUESAS", "Hamburguesas del menú");
        crearSiNoExiste("COMBOS", "Combos de productos");
        crearSiNoExiste("PIQUEOS", "Piqueos y acompañamientos");
        crearSiNoExiste("BEBIDAS", "Bebidas del menú");
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
}
