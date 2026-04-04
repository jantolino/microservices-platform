package edu.market.notification.application.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

import edu.market.notification.domain.enums.EventStatusType;

/**
 * Value Object para representar el estado de una notificación en las respuestas de los casos de uso.
 * Este VO evita exponer directamente los enums del dominio fuera de la capa de aplicación.
 */
public record NotificationStatusResponse(
    
    UUID notificationId,
    EventStatusType status,
    LocalDateTime lastUpdated,
    String errorMessage,
    int retryCount,
    boolean canRetry
) {}
