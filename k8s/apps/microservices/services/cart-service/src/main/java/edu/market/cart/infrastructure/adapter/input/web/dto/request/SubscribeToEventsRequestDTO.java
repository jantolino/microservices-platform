package edu.market.notification.infrastructure.adapter.input.web.dto.request;

import edu.market.notification.domain.enums.SourceServiceType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para recibir solicitudes de suscripción a eventos específicos de otros microservicios.
 * Este DTO está documentado con anotaciones de Swagger para la generación de documentación de API.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to subscribe to events from other microservices")
public class SubscribeToEventsRequestDTO {
    
    @NotBlank(message = "eventType required")
    @Schema(description = "Type of event to subscribe to", 
            example = "USER_REGISTERED", 
            required = true)
    private String eventType;
    
    @NotBlank(message = "sourceService required")
    @Schema(description = "Service that originates the event", 
            example = "USER_SERVICE", 
            required = true)
    private SourceServiceType sourceService;
}
