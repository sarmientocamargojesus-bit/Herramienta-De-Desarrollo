package com.fastfood.fast_food_backend.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.fastfood.fast_food_backend.entities.Producto;

public interface ProductoRepository extends JpaRepository<Producto, Long> {
}
