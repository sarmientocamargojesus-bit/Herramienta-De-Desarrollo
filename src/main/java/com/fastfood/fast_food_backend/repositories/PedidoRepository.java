package com.fastfood.fast_food_backend.repositories;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.repository.query.Param;

import com.fastfood.fast_food_backend.entities.Pedido;

import jakarta.persistence.LockModeType;

public interface PedidoRepository extends JpaRepository<Pedido, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Pedido p WHERE p.id = :id")
    Optional<Pedido> bloquearPorId(@Param("id") Long id);

    List<Pedido> findByUsuarioId(Long usuarioId);

    List<Pedido> findByEstadoIgnoreCase(String estado);

    @Query("SELECT SUM(p.total) FROM Pedido p WHERE UPPER(p.estado) = UPPER(:estado)")
    BigDecimal sumarTotalPorEstado(@Param("estado") String estado);
}
