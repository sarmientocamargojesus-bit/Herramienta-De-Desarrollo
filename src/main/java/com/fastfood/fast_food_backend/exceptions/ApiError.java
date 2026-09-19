package com.fastfood.fast_food_backend.exceptions;

import java.time.LocalDateTime;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Modelo de respuesta de error estandarizado para todos los endpoints de la API.
 * 
 * Proporciona una estructura consistente para comunicar errores al cliente,
 * incluyendo información del estado, mensaje y validaciones específicas.
 * 
 * @author FastFood Backend Team
 * @version 1.0
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class ApiError {

    private Integer codigo;
    private String estado;
    private String mensaje;
    private String ruta;
    private LocalDateTime timestamp;
    private Map<String, String> validaciones;

    /**
     * Constructor completo para ApiError.
     *
     * @param codigo        El código HTTP de estado
     * @param estado        La descripción del estado HTTP
     * @param mensaje       El mensaje de error descriptivo
     * @param ruta          La ruta del endpoint donde ocurrió el error
     * @param timestamp     La fecha y hora del error
     * @param validaciones  Mapa de errores de validación por campo
     */
    public ApiError(Integer codigo, String estado, String mensaje, String ruta, 
                    LocalDateTime timestamp, Map<String, String> validaciones) {
        this.codigo = codigo;
        this.estado = estado;
        this.mensaje = mensaje;
        this.ruta = ruta;
        this.timestamp = timestamp;
        this.validaciones = validaciones;
    }

    // Getters y Setters
    
    /**
     * Obtiene el código HTTP de estado.
     *
     * @return El código de estado HTTP
     */
    public Integer getCodigo() {
        return codigo;
    }

    /**
     * Establece el código HTTP de estado.
     *
     * @param codigo El código de estado HTTP
     */
    public void setCodigo(Integer codigo) {
        this.codigo = codigo;
    }

    /**
     * Obtiene la descripción del estado HTTP.
     *
     * @return El estado HTTP
     */
    public String getEstado() {
        return estado;
    }

    /**
     * Establece la descripción del estado HTTP.
     *
     * @param estado El estado HTTP
     */
    public void setEstado(String estado) {
        this.estado = estado;
    }

    /**
     * Obtiene el mensaje de error descriptivo.
     *
     * @return El mensaje de error
     */
    public String getMensaje() {
        return mensaje;
    }

    /**
     * Establece el mensaje de error descriptivo.
     *
     * @param mensaje El mensaje de error
     */
    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    /**
     * Obtiene la ruta del endpoint donde ocurrió el error.
     *
     * @return La ruta del endpoint
     */
    public String getRuta() {
        return ruta;
    }

    /**
     * Establece la ruta del endpoint donde ocurrió el error.
     *
     * @param ruta La ruta del endpoint
     */
    public void setRuta(String ruta) {
        this.ruta = ruta;
    }

    /**
     * Obtiene la fecha y hora del error.
     *
     * @return El timestamp del error
     */
    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    /**
     * Establece la fecha y hora del error.
     *
     * @param timestamp El timestamp del error
     */
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * Obtiene el mapa de errores de validación por campo.
     *
     * @return Los errores de validación
     */
    public Map<String, String> getValidaciones() {
        return validaciones;
    }

    /**
     * Establece el mapa de errores de validación por campo.
     *
     * @param validaciones Los errores de validación
     */
    public void setValidaciones(Map<String, String> validaciones) {
        this.validaciones = validaciones;
    }

    @Override
    public String toString() {
        return "ApiError{" +
                "codigo=" + codigo +
                ", estado='" + estado + '\'' +
                ", mensaje='" + mensaje + '\'' +
                ", ruta='" + ruta + '\'' +
                ", timestamp=" + timestamp +
                ", validaciones=" + validaciones +
                '}';
    }
}
