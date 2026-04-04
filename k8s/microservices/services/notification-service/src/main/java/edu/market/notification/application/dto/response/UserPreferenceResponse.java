package edu.market.notification.application.dto.response;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import edu.market.notification.domain.enums.NotificationChannelType;

/**
 * Value Object para representar las preferencias de usuario en las respuestas de los casos de uso.
 * Este VO evita exponer directamente las entidades de dominio fuera de la capa de aplicación.
 */
public record UserPreferenceResponse(
    UUID userId,
    boolean globalOptOut,
    Set<NotificationChannelType> enabledChannels,
    Map<String, Set<String>> eventTypePreferences,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
