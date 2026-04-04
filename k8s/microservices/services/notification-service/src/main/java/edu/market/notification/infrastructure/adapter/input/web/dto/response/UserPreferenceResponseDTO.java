package edu.market.notification.infrastructure.adapter.input.web.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import edu.market.notification.domain.enums.NotificationChannelType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * DTO para respuestas de preferencias de usuario a través de la API REST.
 * Este DTO está documentado con anotaciones de Swagger para la generación de documentación de API.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "User notification preferences")
public record UserPreferenceResponseDTO(
    /**
     * Identificador único del usuario.
     */
    @Schema(description = "Unique identifier of the user", 
            example = "123e4567-e89b-12d3-a456-426614174000")
    UUID userId,
    
    /**
     * Indica si el usuario ha desactivado todas las notificaciones.
     */
    @Schema(description = "Whether the user has opted out of all notifications", 
            example = "false")
    boolean globalOptOut,
    
    /**
     * Canales de notificación habilitados para el usuario.
     */
    @Schema(description = "Notification channels enabled for the user", 
            example = "[\"EMAIL\", \"SMS\", \"PUSH\"]")
    Set<NotificationChannelType> enabledChannels,
    
    /**
     * Preferencias por tipo de evento.
     */
    @Schema(description = "Preferences by event type, with channels enabled for each event")
    Map<String, Set<String>> eventTypePreferences,
    
    /**
     * Fecha y hora de creación de las preferencias.
     */
    @Schema(description = "Date and time when the preferences were created", 
            example = "2025-06-15T10:30:00")
    LocalDateTime createdAt,
    
    /**
     * Fecha y hora de última actualización de las preferencias.
     */
    @Schema(description = "Date and time when the preferences were last updated", 
            example = "2025-07-09T15:45:00")
    LocalDateTime updatedAt
) {}
