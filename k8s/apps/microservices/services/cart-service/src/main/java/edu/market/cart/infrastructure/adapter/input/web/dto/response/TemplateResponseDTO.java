package edu.market.notification.infrastructure.adapter.input.web.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * DTO para respuestas de plantillas de notificación a través de la API REST.
 * Este DTO está documentado con anotaciones de Swagger para la generación de documentación de API.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Template details")
public record TemplateResponseDTO(
    /**
     * Identificador único de la plantilla.
     */
    @Schema(description = "Unique identifier of the template", 
            example = "welcome-email")
    String templateId,
    
    /**
     * Tipo de la plantilla.
     */
    @Schema(description = "Type of the template", 
            example = "EMAIL")
    String templateType,
    
    /**
     * Nombre de la plantilla.
     */
    @Schema(description = "Name of the template", 
            example = "Welcome Email Template")
    String name,
    
    /**
     * Asunto de la plantilla.
     */
    @Schema(description = "Subject of the template", 
            example = "Welcome to our platform")
    String subject,
    
    /**
     * Contenido de la plantilla.
     */
    @Schema(description = "Content of the template", 
            example = "<html><body>Welcome {{name}}!</body></html>")
    String content,
    
    /**
     * Tipo de contenido de la plantilla.
     */
    @Schema(description = "Content type of the template", 
            example = "text/html")
    String contentType,
    
    /**
     * Metadatos adicionales de la plantilla.
     */
    @Schema(description = "Additional metadata about the template")
    Map<String, Object> metadata,
    
    /**
     * Fecha y hora de creación de la plantilla.
     */
    @Schema(description = "Date and time when the template was created", 
            example = "2025-07-01T10:30:00")
    LocalDateTime createdAt,
    
    /**
     * Fecha y hora de última actualización de la plantilla.
     */
    @Schema(description = "Date and time when the template was last updated", 
            example = "2025-07-09T15:45:00")
    LocalDateTime updatedAt
) {}
