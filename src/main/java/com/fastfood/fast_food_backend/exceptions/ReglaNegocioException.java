package com.fastfood.fast_food_backend.exceptions;

/**
 * Excepción lanzada cuando una operación viola las reglas de negocio de la aplicación.
 * 
 * Esta excepción se utiliza para validaciones de lógica de negocio que no pueden
 * ser capturadas por validaciones estándar, resultando en una respuesta HTTP 409 Conflict.
 * 
 * Ejemplos de uso:
 * - Intento de crear un pedido sin productos disponibles
 * - Intento de cambiar el estado de un pedido a un estado inválido
 * - Intento de aplicar un descuento no permitido
 * - Intento de procesar un pago con fondos insuficientes
 * 
 * @author FastFood Backend Team
 * @version 1.0
 */
public class ReglaNegocioException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * Constructor con mensaje descriptivo.
     *
     * @param mensaje El mensaje que describe la violación de la regla de negocio
     */
    public ReglaNegocioException(String mensaje) {
        super(mensaje);
    }

    /**
     * Constructor con mensaje descriptivo y causa raíz.
     *
     * @param mensaje El mensaje que describe la violación de la regla de negocio
     * @param causa   La causa raíz de la excepción
     */
    public ReglaNegocioException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }

    /**
     * Constructor para formar mensajes dinámicos con formato.
     * Útil para incluir detalles contextuales en el mensaje de error.
     *
     * @param formato El formato del mensaje (ej: "No se puede cambiar el estado de '%s' a '%s'")
     * @param args    Los argumentos para el formato
     * @return Una nueva instancia de ReglaNegocioException
     */
    public static ReglaNegocioException conFormato(String formato, Object... args) {
        return new ReglaNegocioException(String.format(formato, args));
    }
}
