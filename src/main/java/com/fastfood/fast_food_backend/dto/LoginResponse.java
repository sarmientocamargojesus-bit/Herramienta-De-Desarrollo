package com.fastfood.fast_food_backend.dto;

public class LoginResponse {

    private final String token;
    private final String tipo;
    private final long expiraEnSegundos;
    private final String correo;
    private final String rol;

    public LoginResponse(String token, long expiraEnSegundos, String correo, String rol) {
        this.token = token;
        this.tipo = "Bearer";
        this.expiraEnSegundos = expiraEnSegundos;
        this.correo = correo;
        this.rol = rol;
    }

    public String getToken() {
        return token;
    }

    public String getTipo() {
        return tipo;
    }

    public long getExpiraEnSegundos() {
        return expiraEnSegundos;
    }

    public String getCorreo() {
        return correo;
    }

    public String getRol() {
        return rol;
    }
}
