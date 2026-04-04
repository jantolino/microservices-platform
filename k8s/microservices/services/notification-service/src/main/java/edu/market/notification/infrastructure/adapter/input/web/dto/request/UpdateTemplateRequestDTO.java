package edu.market.notification.infrastructure.adapter.input.web.dto.request;

import java.util.Map;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para recibir solicitudes de actualización de plantillas de notificación a través de la API REST.
 * Este DTO está documentado con anotaciones de Swagger para la generación de documentación de API.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to update an existing notification template")
public class UpdateTemplateRequestDTO {
    
    @NotBlank(message = "userUuid required")
    @Schema(description = "Unique identifier of the user",             
            required = true)
    private String userUuid;
    
    @NotBlank(message = "templateType required")
    @Schema(description = "Template type (email, sms, push, etc.)", 
            example = "EMAIL", 
            required = true)
    private String templateType;
    
    @NotBlank(message = "name required")
    @Schema(description = "Descriptive name of the template", 
            example = "Updated welcome template", 
            required = true)
    private String name;
    
    @NotBlank(message = "subject required")
    @Schema(description = "Subject to be used in the notification", 
            example = "Welcome to our renewed platform", 
            required = true)
    private String subject;
    
    @NotBlank(message = "content required")
    @Schema(description = "Template content, can include variables with {{variable}} format", 
            example = "Hello {{name}}, welcome to our renewed platform.", 
            required = true)
    private String content;
    
    @NotBlank(message = "contentType required")
    @Schema(description = "Content type (text/plain, text/html, etc.)", 
            example = "text/html", 
            required = true)
    private String contentType;
    
    @Schema(description = "Additional metadata for the template", 
            example = "{\"version\": \"2.0\", \"category\": \"welcome\", \"lastUpdated\": \"2025-07-09\"}")
    private Map<String, Object> metadata;
}
