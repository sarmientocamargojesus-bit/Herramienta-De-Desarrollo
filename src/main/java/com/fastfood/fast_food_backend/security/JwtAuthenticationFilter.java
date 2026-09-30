package com.fastfood.fast_food_backend.security;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.fastfood.fast_food_backend.exceptions.ApiError;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import tools.jackson.databind.ObjectMapper;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String PREFIJO_BEARER = "Bearer ";

    private final JwtService jwtService;
    private final ObjectMapper objectMapper;

    public JwtAuthenticationFilter(JwtService jwtService, ObjectMapper objectMapper) {
        this.jwtService = jwtService;
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authorization == null || !authorization.regionMatches(true, 0, PREFIJO_BEARER, 0,
                PREFIJO_BEARER.length())) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            String token = authorization.substring(PREFIJO_BEARER.length()).trim();
            JwtService.JwtClaims claims = jwtService.validarYLeer(token);
            var authority = new SimpleGrantedAuthority("ROLE_" + claims.rol());
            var authentication = UsernamePasswordAuthenticationToken.authenticated(
                    claims.correo(), token, List.of(authority));
            SecurityContextHolder.getContext().setAuthentication(authentication);
            filterChain.doFilter(request, response);
        } catch (IllegalArgumentException exception) {
            SecurityContextHolder.clearContext();
            escribirRespuestaNoAutorizada(request, response);
        }
    }

    private void escribirRespuestaNoAutorizada(
            HttpServletRequest request,
            HttpServletResponse response) throws IOException {
        HttpStatus status = HttpStatus.UNAUTHORIZED;
        ApiError error = new ApiError(
                status.value(),
                status.getReasonPhrase(),
                "El token JWT es inválido o está vencido",
                request.getRequestURI(),
                LocalDateTime.now(),
                Map.of());
        response.setStatus(status.value());
        response.setHeader(HttpHeaders.WWW_AUTHENTICATE, "Bearer");
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        objectMapper.writeValue(response.getWriter(), error);
    }
}
