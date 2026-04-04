package edu.market.notification.infrastructure.adapter.input.web.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * DTO para recibir solicitudes de actualización de preferencias de notificación de usuarios a través de la API REST.
 * Este DTO está documentado con anotaciones de Swagger para la generación de documentación de API.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to update user notification preferences")
public class UserPreferencesRequestDTO {
    
    /**
     * Indica si el usuario desea recibir notificaciones en general.
     */
    @NotNull(message = "notificationsEnabled required")
    @Schema(description = "Indicates if the user wants to receive notifications in general", 
            example = "true", 
            required = true)
    private Boolean notificationsEnabled;
    
    /**
     * Lista de canales de notificación habilitados para el usuario.
     */
    @NotEmpty(message = "enabledChannels required")
    @Schema(description = "List of notification channels enabled for the user", 
            example = "[\"EMAIL\", \"SMS\", \"PUSH\"]", 
            required = true)
    private List<String> enabledChannels;
    
    /**
     * Mapa de tipos de eventos y si están habilitados para el usuario.
     */
    @NotNull(message = "eventPreferences required")
    @Schema(description = "Map of event types and whether they are enabled for the user", 
            example = "{\"ORDER_CREATED\": true, \"PAYMENT_RECEIVED\": false}", 
            required = true)
    private Map<String, Boolean> eventPreferences;
    
    /**
     * Configuración específica por canal y tipo de evento.
     */
    @Schema(description = "Specific configuration by channel and event type", 
            example = "{\"EMAIL\": {\"ORDER_CREATED\": true}, \"SMS\": {\"ORDER_CREATED\": false}}")
    private Map<String, Map<String, Boolean>> channelEventPreferences;
}
