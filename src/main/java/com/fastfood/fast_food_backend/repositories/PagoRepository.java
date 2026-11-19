package com.fastfood.fast_food_backend.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.fastfood.fast_food_backend.entities.Pago;

public interface PagoRepository extends JpaRepository<Pago, Long> {

    Optional<Pago> findByPedidoId(Long pedidoId);

    boolean existsByPedidoId(Long pedidoId);

    // Cada pago con su pedido y el cliente del pedido en una sola consulta.
    @Query("SELECT pg FROM Pago pg JOIN FETCH pg.pedido pe JOIN FETCH pe.usuario")
    List<Pago> listarConPedido();
}
