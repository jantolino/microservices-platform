package edu.market.notification.application.dto.response;

import java.util.List;

/**
 * Value Object para el resultado de la consulta del historial de notificaciones.
 */
public record NotificationHistoryResult(
    List<NotificationResponse> notifications,
    int totalCount,
    int limit,
    int offset,
    boolean hasMore
) {}
