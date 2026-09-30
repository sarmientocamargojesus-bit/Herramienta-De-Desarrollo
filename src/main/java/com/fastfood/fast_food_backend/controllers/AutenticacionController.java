package com.fastfood.fast_food_backend.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fastfood.fast_food_backend.dto.LoginRequest;
import com.fastfood.fast_food_backend.dto.LoginResponse;
import com.fastfood.fast_food_backend.services.AutenticacionService;

@RestController
@RequestMapping("/api/auth")
public class AutenticacionController {

    private final AutenticacionService autenticacionService;

    public AutenticacionController(AutenticacionService autenticacionService) {
        this.autenticacionService = autenticacionService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> iniciarSesion(@RequestBody LoginRequest solicitud) {
        return ResponseEntity.ok(autenticacionService.iniciarSesion(solicitud));
    }
}
