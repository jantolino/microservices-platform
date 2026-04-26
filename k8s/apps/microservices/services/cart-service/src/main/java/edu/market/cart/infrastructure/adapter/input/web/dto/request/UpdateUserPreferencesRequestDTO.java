package edu.market.notification.infrastructure.adapter.input.web.dto.request;

import edu.market.notification.domain.enums.NotificationChannelType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * DTO para recibir solicitudes de actualización de preferencias de notificación de un usuario a través de la API REST.
 * Este DTO está documentado con anotaciones de Swagger para la generación de documentación de API.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to update user notification preferences")
public class UpdateUserPreferencesRequestDTO {
    
    @NotNull(message = "userId required")
    @Schema(description = "Unique identifier of the user", 
            example = "123e4567-e89b-12d3-a456-426614174000", 
            required = true)
    private UUID userId;
    
    @Schema(description = "Whether the user has opted out of all notifications", 
            example = "false")
    private boolean globalOptOut;
    
    @Schema(description = "List of notification channels that are disabled for this user", 
            example = "[\"EMAIL\", \"SMS\"]")
    private Set<NotificationChannelType> disabledChannels;
    
    @Schema(description = "Map of event types to their enabled channels", 
            example = "{\"ORDER_CREATED\": [\"EMAIL\", \"PUSH\"]}")
    private Map<String, Set<String>> eventTypePreferences;
    
    @Schema(description = "User's timezone for scheduling notifications", 
            example = "America/New_York")
    private String timezone;
    
    @Schema(description = "User's preferred language for notifications", 
            example = "es")
    private String preferredLanguage;
    
    @Schema(description = "Additional user-specific settings", 
            example = "{\"dailyDigest\": \"true\", \"quietHours\": \"22:00-08:00\"}")
    private Map<String, String> additionalSettings;
}
