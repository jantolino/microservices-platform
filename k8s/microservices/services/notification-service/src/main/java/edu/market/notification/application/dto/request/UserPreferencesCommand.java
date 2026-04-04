package edu.market.notification.application.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.util.List;
import java.util.Map;

/**
 * Comando para actualizar las preferencias de notificación de un usuario.
 */
@Builder
public record UserPreferencesCommand(
    /**
     * Indica si el usuario desea recibir notificaciones en general.
     */
    @NotNull(message = "El estado de notificaciones no puede ser nulo")
    Boolean notificationsEnabled,
    
    /**
     * Lista de canales de notificación habilitados para el usuario.
     * Ejemplo: ["EMAIL", "SMS", "PUSH"]
     */
    @NotEmpty(message = "Debe especificar al menos un canal de notificación")
    List<String> enabledChannels,
    
    /**
     * Mapa de tipos de eventos y si están habilitados para el usuario.
     * Ejemplo: {"ORDER_CREATED": true, "PAYMENT_RECEIVED": false}
     */
    @NotNull(message = "Las preferencias de eventos no pueden ser nulas")
    Map<String, Boolean> eventPreferences,
    
    /**
     * Configuración específica por canal y tipo de evento.
     * Ejemplo: {"EMAIL": {"ORDER_CREATED": true}, "SMS": {"ORDER_CREATED": false}}
     */
    Map<String, Map<String, Boolean>> channelEventPreferences
) {
}
