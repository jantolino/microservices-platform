package edu.market.notification.infrastructure.adapter.input.web.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para recibir solicitudes de búsqueda de plantillas de notificación a través de la API REST.
 * Este DTO está documentado con anotaciones de Swagger para la generación de documentación de API.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to search for notification templates")
public class GetTemplatesRequestDTO {
    
    @NotBlank(message = "userUuid required")
    @Schema(description = "User UUID", required = true)
    private String userUuid;

    @NotBlank(message = "templateId required")
    @Schema(description = "Unique identifier of the template", 
            example = "welcome-template", 
            required = true)
    private String templateId;
    
    @NotBlank(message = "templateType required")
    @Schema(description = "Type of the template", 
            example = "EMAIL", 
            required = true)
    private String templateType;
}
