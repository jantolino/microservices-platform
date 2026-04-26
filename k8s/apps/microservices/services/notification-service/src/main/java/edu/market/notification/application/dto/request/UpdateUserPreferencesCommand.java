package edu.market.notification.application.dto.request;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

import edu.market.notification.domain.enums.NotificationChannelType;

import jakarta.validation.constraints.NotNull;

/**
 * Command para actualizar las preferencias de notificación de un usuario.
 *
 * Estas anotaciones de validación son estándares de Java (JSR 303/380) y no introducen una dependencia
 * de un framework específico de infraestructura.
 *
 * Las validaciones se aplican en la capa de aplicación antes de que los datos lleguen al dominio,
 * actuando como un filtro inicial que protege la integridad del modelo de dominio.
 */
public record UpdateUserPreferencesCommand(
    
    @NotNull(message = "userId required")
    UUID userId,
    
    boolean globalOptOut,
    
    Set<NotificationChannelType> disabledChannels,
    
    Map<String, Set<String>> eventTypePreferences,
    
    String timezone,
    
    String preferredLanguage,
    
    Map<String, String> additionalSettings
) {}
