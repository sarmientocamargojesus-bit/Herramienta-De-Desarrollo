package com.fastfood.fast_food_backend.repositories;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.fastfood.fast_food_backend.entities.Pedido;

public interface PedidoRepository extends JpaRepository<Pedido, Long> {

    List<Pedido> findByUsuarioId(Long usuarioId);

    List<Pedido> findByEstadoIgnoreCase(String estado);

    @Query("SELECT SUM(p.total) FROM Pedido p WHERE UPPER(p.estado) = UPPER(:estado)")
    BigDecimal sumarTotalPorEstado(@Param("estado") String estado);
}
