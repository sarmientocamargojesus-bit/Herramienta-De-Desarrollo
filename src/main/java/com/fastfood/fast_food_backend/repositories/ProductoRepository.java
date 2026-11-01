package com.fastfood.fast_food_backend.repositories;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;
import java.util.Optional;

import com.fastfood.fast_food_backend.entities.Producto;

public interface ProductoRepository extends JpaRepository<Producto, Long> {

    boolean existsByCategoriaReferenciaId(Long categoriaId);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("UPDATE Producto p SET p.categoria = :nombre WHERE p.categoriaReferencia.id = :categoriaId")
    int actualizarNombreCategoria(@Param("categoriaId") Long categoriaId, @Param("nombre") String nombre);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Producto p WHERE p.id = :id")
    Optional<Producto> bloquearPorId(@Param("id") Long id);

    @Query("SELECT p FROM Producto p WHERE LOWER(p.nombre) LIKE LOWER(CONCAT('%', :nombre, '%'))")
    List<Producto> buscarPorNombre(@Param("nombre") String nombre);

    @Query("SELECT p FROM Producto p WHERE p.stock > 0")
    List<Producto> buscarConStockDisponible();

    @Query("SELECT p FROM Producto p WHERE p.precio BETWEEN :precioMinimo AND :precioMaximo AND p.stock > 0")
    List<Producto> buscarPorRangoDePrecios(
            @Param("precioMinimo") BigDecimal precioMinimo,
            @Param("precioMaximo") BigDecimal precioMaximo);

    @Query("SELECT p FROM Producto p WHERE NOT EXISTS (SELECT i FROM Inventario i WHERE i.producto = p) ORDER BY p.id")
    List<Producto> buscarSinInventario();

    @Query("SELECT p FROM Producto p ORDER BY p.precio DESC")
    List<Producto> ordenarPorPrecioDescendente();
}
