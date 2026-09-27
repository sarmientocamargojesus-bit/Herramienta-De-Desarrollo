package com.fastfood.fast_food_backend.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.fastfood.fast_food_backend.entities.Inventario;

public interface InventarioRepository extends JpaRepository<Inventario, Long> {

    Optional<Inventario> findByProductoId(Long productoId);

    boolean existsByProductoId(Long productoId);

    @Query("SELECT i FROM Inventario i WHERE i.producto.stock <= i.stockMinimo ORDER BY i.producto.nombre")
    List<Inventario> buscarProductosConStockBajo();
}
