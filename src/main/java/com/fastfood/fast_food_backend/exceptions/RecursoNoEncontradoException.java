package com.fastfood.fast_food_backend.exceptions;

/**
 * Excepción lanzada cuando un recurso solicitado no es encontrado.
 * 
 * Esta excepción se utiliza cuando el usuario intenta acceder a un recurso
 * que no existe en la base de datos o no está disponible, resultando en
 * una respuesta HTTP 404 Not Found.
 * 
 * @author FastFood Backend Team
 * @version 1.0
 */
public class RecursoNoEncontradoException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * Constructor con mensaje descriptivo.
     *
     * @param mensaje El mensaje que describe por qué no se encontró el recurso
     */
    public RecursoNoEncontradoException(String mensaje) {
        super(mensaje);
    }

    /**
     * Constructor con mensaje descriptivo y causa raíz.
     *
     * @param mensaje El mensaje que describe por qué no se encontró el recurso
     * @param causa   La causa raíz de la excepción
     */
    public RecursoNoEncontradoException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }

    /**
     * Constructor para formar mensajes dinámicos con formato.
     * Útil para incluir detalles como el ID del recurso no encontrado.
     *
     * @param formato El formato del mensaje (ej: "Categoría con ID %d no encontrada")
     * @param args    Los argumentos para el formato
     * @return Una nueva instancia de RecursoNoEncontradoException
     */
    public static RecursoNoEncontradoException conFormato(String formato, Object... args) {
        return new RecursoNoEncontradoException(String.format(formato, args));
    }
}
