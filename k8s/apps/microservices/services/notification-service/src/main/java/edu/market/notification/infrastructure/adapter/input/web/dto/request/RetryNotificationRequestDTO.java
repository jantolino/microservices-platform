package edu.market.notification.infrastructure.adapter.input.web.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * DTO para recibir solicitudes de reintento de envío de notificaciones fallidas a través de la API REST.
 * Este DTO está documentado con anotaciones de Swagger para la generación de documentación de API.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to retry a failed notification")
public class RetryNotificationRequestDTO {
    
    @NotNull(message = "notificationId required")
    @Schema(description = "Unique identifier of the notification to retry", 
            example = "123e4567-e89b-12d3-a456-426614174000", 
            required = true)
    private UUID notificationId;
}
