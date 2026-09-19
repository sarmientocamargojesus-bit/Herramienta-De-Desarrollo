package com.fastfood.fast_food_backend.exceptions;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Manejador global centralizado de excepciones para la aplicación.
 * 
 * Proporciona un manejo consistente de errores en todos los endpoints REST,
 * retornando respuestas de error estandarizadas en formato JSON con información
 * completa sobre el problema ocurrido.
 * 
 * @author FastFood Backend Team
 * @version 1.0
 */
@RestControllerAdvice
public class ManejadorGlobalExcepciones {

    private static final Logger logger = LoggerFactory.getLogger(ManejadorGlobalExcepciones.class);

    // Mensajes de error constantes
    private static final String DATOS_INVALIDOS = "Los datos enviados no son válidos";
    private static final String JSON_INVALIDO = "El cuerpo de la solicitud es inválido o contiene un valor no permitido";
    private static final String VIOLACION_RESTRICCION = "La operación viola una restricción de datos";
    private static final String ENDPOINT_NO_ENCONTRADO = "El endpoint solicitado no existe";
    private static final String ERROR_INTERNO = "Ha ocurrido un error interno en el servidor";
    private static final String PARAMETRO_INVALIDO = "El parámetro de la solicitud es inválido o tiene un tipo incorrecto";

    /**
     * Maneja excepciones de recurso no encontrado.
     *
     * @param exception La excepción capturada
     * @param request   La solicitud HTTP
     * @return ResponseEntity con estado 404 NOT FOUND
     */
    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<ApiError> manejarNoEncontrado(
            RecursoNoEncontradoException exception, 
            HttpServletRequest request) {
        logger.warn("Recurso no encontrado: {}", exception.getMessage());
        return construirRespuesta(HttpStatus.NOT_FOUND, exception.getMessage(), request, Map.of());
    }

    /**
     * Maneja excepciones de violaciones de reglas de negocio y restricciones de datos.
     *
     * @param exception La excepción capturada
     * @param request   La solicitud HTTP
     * @return ResponseEntity con estado 409 CONFLICT
     */
    @ExceptionHandler({ReglaNegocioException.class, DataIntegrityViolationException.class})
    public ResponseEntity<ApiError> manejarConflicto(
            Exception exception, 
            HttpServletRequest request) {
        String mensaje = exception instanceof ReglaNegocioException
                ? exception.getMessage()
                : VIOLACION_RESTRICCION;
        logger.warn("Conflicto en la operación: {}", mensaje);
        return construirRespuesta(HttpStatus.CONFLICT, mensaje, request, Map.of());
    }

    /**
     * Maneja excepciones de validación de argumentos en métodos.
     * Extrae información detallada de los errores de validación por campo.
     *
     * @param exception La excepción capturada
     * @param request   La solicitud HTTP
     * @return ResponseEntity con estado 400 BAD REQUEST y detalle de errores
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> manejarValidacion(
            MethodArgumentNotValidException exception, 
            HttpServletRequest request) {
        Map<String, String> errores = new LinkedHashMap<>();
        exception.getBindingResult().getFieldErrors()
                .forEach(error -> errores.putIfAbsent(
                        error.getField(), 
                        error.getDefaultMessage()
                ));
        logger.debug("Errores de validación detectados: {}", errores);
        return construirRespuesta(HttpStatus.BAD_REQUEST, DATOS_INVALIDOS, request, errores);
    }

    /**
     * Maneja excepciones de JSON inválido o malformado.
     *
     * @param exception La excepción capturada
     * @param request   La solicitud HTTP
     * @return ResponseEntity con estado 400 BAD REQUEST
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> manejarJsonInvalido(
            HttpMessageNotReadableException exception, 
            HttpServletRequest request) {
        logger.warn("Formato JSON inválido en la solicitud: {}", exception.getMessage());
        return construirRespuesta(HttpStatus.BAD_REQUEST, JSON_INVALIDO, request, Map.of());
    }

    /**
     * Maneja excepciones de tipo de parámetro incorrecto.
     *
     * @param exception La excepción capturada
     * @param request   La solicitud HTTP
     * @return ResponseEntity con estado 400 BAD REQUEST
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiError> manejarTipoParametroInvalido(
            MethodArgumentTypeMismatchException exception, 
            HttpServletRequest request) {
        String mensaje = String.format(
                "El parámetro '%s' debe ser de tipo %s",
                exception.getName(),
                exception.getRequiredType() != null ? exception.getRequiredType().getSimpleName() : "desconocido"
        );
        logger.warn("Tipo de parámetro incorrecto: {}", mensaje);
        return construirRespuesta(HttpStatus.BAD_REQUEST, PARAMETRO_INVALIDO, request, Map.of());
    }

    /**
     * Maneja excepciones cuando no se encuentra el endpoint solicitado.
     *
     * @param exception La excepción capturada
     * @param request   La solicitud HTTP
     * @return ResponseEntity con estado 404 NOT FOUND
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ApiError> manejarEndpointNoEncontrado(
            NoHandlerFoundException exception, 
            HttpServletRequest request) {
        logger.warn("Endpoint no encontrado: {} {}", exception.getHttpMethod(), exception.getRequestURL());
        return construirRespuesta(HttpStatus.NOT_FOUND, ENDPOINT_NO_ENCONTRADO, request, Map.of());
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiError> manejarRecursoNoEncontrado(
            NoResourceFoundException exception,
            HttpServletRequest request) {
        logger.warn("Ruta no encontrada: {}", request.getRequestURI());
        return construirRespuesta(HttpStatus.NOT_FOUND, ENDPOINT_NO_ENCONTRADO, request, Map.of());
    }

    /**
     * Manejador genérico para excepciones no capturadas.
     * Se ejecuta como último recurso para evitar que errores no controlados expongan información sensible.
     *
     * @param exception La excepción capturada
     * @param request   La solicitud HTTP
     * @return ResponseEntity con estado 500 INTERNAL SERVER ERROR
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> manejarErrorGeneral(
            Exception exception, 
            HttpServletRequest request) {
        logger.error("Error no controlado en la aplicación", exception);
        return construirRespuesta(HttpStatus.INTERNAL_SERVER_ERROR, ERROR_INTERNO, request, Map.of());
    }

    /**
     * Construye una respuesta de error estandarizada.
     * 
     * Método privado que centraliza la construcción de objetos ApiError para garantizar
     * consistencia en todas las respuestas de error de la aplicación.
     *
     * @param status       El código HTTP de estado
     * @param mensaje      El mensaje de error descriptivo
     * @param request      La solicitud HTTP original
     * @param validaciones Mapa de errores de validación por campo (vacío si no aplica)
     * @return ResponseEntity con el ApiError como cuerpo
     */
    private ResponseEntity<ApiError> construirRespuesta(
            HttpStatus status, 
            String mensaje, 
            HttpServletRequest request, 
            Map<String, String> validaciones) {
        
        ApiError error = new ApiError(
                status.value(),
                status.getReasonPhrase(),
                mensaje,
                request.getRequestURI(),
                LocalDateTime.now(),
                validaciones
        );
        
        return ResponseEntity.status(status).body(error);
    }
}
