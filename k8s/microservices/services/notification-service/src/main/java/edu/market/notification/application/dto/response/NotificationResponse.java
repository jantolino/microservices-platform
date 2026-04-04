package edu.market.notification.application.dto.response;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

import edu.market.notification.domain.enums.NotificationChannelType;
import edu.market.notification.domain.enums.NotificationPriorityType;
import edu.market.notification.domain.enums.EventStatusType;

/**
 * Objeto de respuesta para representar una notificación en las respuestas de los casos de uso.
 * Este objeto evita exponer directamente las entidades de dominio fuera de la capa de aplicación.
 * Proporciona una representación clara y consistente para las respuestas de operaciones.
 */
public record NotificationResponse(
    UUID id,
    String subject,
    String content,
    String recipientId,
    String recipientEmail,
    Set<NotificationChannelType> channels,
    NotificationPriorityType priority,
    EventStatusType status,
    LocalDateTime createdAt,
    LocalDateTime scheduledFor,
    LocalDateTime sentAt,
    int retryCount,
    String errorMessage,
    String sourceService
) {}
