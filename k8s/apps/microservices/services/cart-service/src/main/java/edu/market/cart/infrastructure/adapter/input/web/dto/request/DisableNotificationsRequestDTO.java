package edu.market.notification.infrastructure.adapter.input.web.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

/**
 * DTO para recibir solicitudes de deshabilitación de notificaciones para un usuario a través de la API REST.
 * Este DTO está documentado con anotaciones de Swagger para la generación de documentación de API.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to disable notifications for a user")
public class DisableNotificationsRequestDTO {
    
    @NotNull(message = "userId required")
    @Schema(description = "Unique identifier of the user", 
            example = "123e4567-e89b-12d3-a456-426614174000", 
            required = true)
    private UUID userId;
    
    @Valid
    @NotNull(message = "eventTypes required")
    @Schema(description = "List of event types to disable", 
            example = "[\"ORDER_CREATED\", \"PAYMENT_RECEIVED\"]", 
            required = true)
    private List<@NotEmpty String> eventTypes;
    
    @NotNull(message = "reason required")
    @Schema(description = "Reason for disabling notifications", 
            example = "User request", 
            required = true)
    private String reason;
}
