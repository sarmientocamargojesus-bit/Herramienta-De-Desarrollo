package com.fastfood.fast_food_backend.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/** Recibe los datos permitidos para crear una cuenta desde el registro público. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record RegistroUsuarioRequest(
        String nombre,
        String correo,
        String contrasena,
        String telefono,
        String direccion,
        Boolean activo) {
}
