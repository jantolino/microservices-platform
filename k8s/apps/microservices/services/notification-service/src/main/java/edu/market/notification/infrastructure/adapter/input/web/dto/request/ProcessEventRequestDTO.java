package edu.market.notification.infrastructure.adapter.input.web.dto.request;

import java.util.Map;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para recibir solicitudes de procesamiento de eventos de dominio a través de la API REST.
 * Contiene la información necesaria para procesar un evento y generar notificaciones.
 * 
 * Este DTO está documentado con anotaciones de Swagger para la generación de documentación de API.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to process a domain event")
public class ProcessEventRequestDTO {
    
    @Schema(description = "Unique identifier of the event", required = true)
    private String eventId;
    
    @NotNull(message = "eventType required")
    @Schema(description = "Type of event to process", example = "USER_REGISTERED", required = true)
    private String eventType;
    
    @NotNull(message = "source required")
    @Schema(description = "Service or component that originated the event", example = "user-service", required = true)
    private String source;
    
    @NotNull(message = "payload required")
    @Schema(description = "Event data in key-value format", required = true)
    private Map<String, Object> payload;
    
    @Schema(description = "Correlation ID for tracing between services")
    private String correlationId;
}
