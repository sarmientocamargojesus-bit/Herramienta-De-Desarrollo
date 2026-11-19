package com.fastfood.fast_food_backend.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.fastfood.fast_food_backend.entities.Inventario;

public interface InventarioRepository extends JpaRepository<Inventario, Long> {

    Optional<Inventario> findByProductoId(Long productoId);

    boolean existsByProductoId(Long productoId);

    // Trae cada inventario con su producto y su categoría en una sola consulta (evita una consulta por producto).
    @Query("SELECT i FROM Inventario i JOIN FETCH i.producto p LEFT JOIN FETCH p.categoriaReferencia")
    List<Inventario> listarConProducto();

    @Query("SELECT i FROM Inventario i JOIN FETCH i.producto p LEFT JOIN FETCH p.categoriaReferencia "
            + "WHERE p.stock <= i.stockMinimo ORDER BY p.nombre")
    List<Inventario> buscarProductosConStockBajo();
}
