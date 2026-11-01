package com.fastfood.fast_food_backend.services;

import java.util.Locale;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import com.fastfood.fast_food_backend.dto.LoginRequest;
import com.fastfood.fast_food_backend.dto.LoginResponse;
import com.fastfood.fast_food_backend.security.JwtService;

@Service
public class AutenticacionService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AutenticacionService(AuthenticationManager authenticationManager, JwtService jwtService) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    public LoginResponse iniciarSesion(LoginRequest solicitud) {
        if (solicitud == null
                || solicitud.getCorreo() == null
                || solicitud.getCorreo().isBlank()
                || solicitud.getContrasena() == null
                || solicitud.getContrasena().isBlank()) {
            throw new IllegalArgumentException("El correo y la contraseña son obligatorios");
        }

        Authentication authentication = authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken.unauthenticated(
                        solicitud.getCorreo().trim().toLowerCase(Locale.ROOT),
                        solicitud.getContrasena()));

        return jwtService.generar(authentication);
    }
}
