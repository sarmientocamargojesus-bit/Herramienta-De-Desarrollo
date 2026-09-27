package com.fastfood.fast_food_backend.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.fastfood.fast_food_backend.entities.Categoria;

public interface CategoriaRepository extends JpaRepository<Categoria, Long> {

    Optional<Categoria> findByNombreIgnoreCase(String nombre);

    List<Categoria> findByActivoTrueOrderByNombreAsc();
}
