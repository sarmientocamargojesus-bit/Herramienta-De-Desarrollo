package com.fastfood.fast_food_backend.security;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import com.fastfood.fast_food_backend.entities.Pago;
import com.fastfood.fast_food_backend.entities.Pedido;
import com.fastfood.fast_food_backend.dto.ProcesarPedidoRequest;
import com.fastfood.fast_food_backend.dto.CrearPedidoRequest;
import com.fastfood.fast_food_backend.repositories.PagoRepository;
import com.fastfood.fast_food_backend.repositories.PedidoRepository;
import com.fastfood.fast_food_backend.repositories.UsuarioRepository;

@Component("autorizacionPedido")
public class AutorizacionPedido {

    private final PedidoRepository pedidoRepository;
    private final PagoRepository pagoRepository;
    private final UsuarioRepository usuarioRepository;

    public AutorizacionPedido(
            PedidoRepository pedidoRepository,
            PagoRepository pagoRepository,
            UsuarioRepository usuarioRepository) {
        this.pedidoRepository = pedidoRepository;
        this.pagoRepository = pagoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    public boolean esPropietario(Long pedidoId, Authentication authentication) {
        if (pedidoId == null || authentication == null) {
            return false;
        }
        return pedidoRepository.findById(pedidoId)
                .map(Pedido::getUsuario)
                .map(usuario -> usuario.getCorreo().equalsIgnoreCase(authentication.getName()))
                .orElse(false);
    }

    public boolean esMismoUsuario(Long usuarioId, Authentication authentication) {
        if (usuarioId == null || authentication == null) {
            return false;
        }
        return usuarioRepository.findById(usuarioId)
                .map(usuario -> usuario.getCorreo().equalsIgnoreCase(authentication.getName()))
                .orElse(false);
    }

    public boolean esPropietarioPago(Long pagoId, Authentication authentication) {
        if (pagoId == null || authentication == null) {
            return false;
        }
        return pagoRepository.findById(pagoId)
                .map(Pago::getPedido)
                .map(Pedido::getUsuario)
                .map(usuario -> usuario.getCorreo().equalsIgnoreCase(authentication.getName()))
                .orElse(false);
    }

    public boolean puedeRegistrarPago(Pago pago, Authentication authentication) {
        return pago != null && pago.getPedido() != null
                && esPropietario(pago.getPedido().getId(), authentication);
    }

    public boolean puedeCrearPedido(CrearPedidoRequest solicitud, Authentication authentication) {
        return solicitud != null && solicitud.usuario() != null
                && esMismoUsuario(solicitud.usuario().id(), authentication);
    }

    public boolean puedeProcesarPedido(ProcesarPedidoRequest solicitud, Authentication authentication) {
        return solicitud != null && esMismoUsuario(solicitud.getUsuarioId(), authentication);
    }
}
