package com.fastfood.fast_food_backend.config;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import com.fastfood.fast_food_backend.exceptions.ApiError;

import tools.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletResponse;

@Configuration
@EnableMethodSecurity
public class SeguridadConfig {

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http, ObjectMapper objectMapper) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.GET, "/api/productos/**", "/api/categorias/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/usuarios").permitAll()
                        .requestMatchers("/api/usuarios/**").hasRole("ADMINISTRADOR")
                        .requestMatchers("/api/productos/**", "/api/categorias/**").hasRole("ADMINISTRADOR")
                        .requestMatchers("/api/inventarios/**").hasAnyRole("EMPLEADO", "ADMINISTRADOR")
                        .requestMatchers("/api/pedidos/**").hasAnyRole("CLIENTE", "EMPLEADO", "ADMINISTRADOR")
                        .requestMatchers("/api/pagos/**").hasAnyRole("CLIENTE", "ADMINISTRADOR")
                        .anyRequest().permitAll())
                .httpBasic(basic -> basic.authenticationEntryPoint((request, response, exception) ->
                        escribirError(response, objectMapper, HttpStatus.UNAUTHORIZED,
                                "Se requiere autenticación con un correo y una contraseña válidos",
                                request.getRequestURI())))
                .exceptionHandling(errors -> errors
                        .authenticationEntryPoint((request, response, exception) ->
                                escribirError(response, objectMapper, HttpStatus.UNAUTHORIZED,
                                        "Se requiere autenticación con un correo y una contraseña válidos",
                                        request.getRequestURI()))
                        .accessDeniedHandler((request, response, exception) ->
                                escribirError(response, objectMapper, HttpStatus.FORBIDDEN,
                                        "No tiene permisos para realizar esta operación",
                                        request.getRequestURI())));

        return http.build();
    }

    private static void escribirError(
            HttpServletResponse response,
            ObjectMapper objectMapper,
            HttpStatus estado,
            String mensaje,
            String ruta) throws IOException {
        response.setStatus(estado.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        ApiError error = new ApiError(
                estado.value(), estado.getReasonPhrase(), mensaje, ruta, LocalDateTime.now(), Map.of());
        objectMapper.writeValue(response.getWriter(), error);
    }
}
