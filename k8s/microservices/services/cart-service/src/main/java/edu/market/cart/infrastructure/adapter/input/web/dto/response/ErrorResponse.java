package edu.market.notification.infrastructure.adapter.input.web.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Estructura estandarizada para respuestas de error en la API.
 * Esta clase proporciona un formato consistente para todas las respuestas de error.
 */
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(
    /**
     * Marca de tiempo cuando ocurrió el error.
     */
    LocalDateTime timestamp,
    
    /**
     * Código de estado HTTP.
     */
    int status,
    
    /**
     * Tipo de error.
     */
    String error,
    
    /**
     * Mensaje descriptivo del error.
     */
    String message,
    
    /**
     * Ruta de la solicitud que generó el error.
     */
    String path,
    
    /**
     * Detalles adicionales del error, como errores de validación por campo.
     */
    Map<String, String> details
) {}
