package com.fastfood.fast_food_backend.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.fastfood.fast_food_backend.entities.DetallePedido;

public interface DetallePedidoRepository extends JpaRepository<DetallePedido, Long> {

    @Query("SELECT d FROM DetallePedido d JOIN d.pedido p WHERE p.id = :pedidoId ORDER BY d.id")
    List<DetallePedido> buscarPorPedidoId(@Param("pedidoId") Long pedidoId);

    @Query("SELECT DISTINCT d.producto.id FROM DetallePedido d WHERE d.pedido.id = :pedidoId ORDER BY d.producto.id")
    List<Long> buscarProductoIdsPorPedidoId(@Param("pedidoId") Long pedidoId);
}
