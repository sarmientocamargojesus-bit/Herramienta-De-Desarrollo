package com.fastfood.fast_food_backend.controllers;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fastfood.fast_food_backend.dto.CheckoutRequest;
import com.fastfood.fast_food_backend.dto.CheckoutResponse;
import com.fastfood.fast_food_backend.services.CheckoutService;

@RestController
@RequestMapping("/api/checkout")
public class CheckoutController {

    private final CheckoutService checkout;

    public CheckoutController(CheckoutService checkout) { this.checkout = checkout; }

    @PostMapping
    public CheckoutResponse confirmar(Authentication autenticacion, @RequestBody CheckoutRequest solicitud) {
        return checkout.confirmar(autenticacion.getName(), solicitud);
    }
}
