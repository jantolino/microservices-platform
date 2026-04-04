package edu.market.notification.application.port.input.notification;

import java.util.UUID;

import edu.market.notification.application.dto.request.ClientContextCommand;
import edu.market.notification.application.dto.response.NotificationResponse;

/**
 * Puerto de caso de uso para reintentar notificaciones fallidas.
 * 
 * Este caso de uso permite reintentar el envío de notificaciones que han fallado
 * en intentos anteriores, dándoles otra oportunidad de ser entregadas.
 */
public interface RetryNotificationUseCasePort {
    
    /**
     * Reintenta el envío de una notificación que ha fallado previamente.
     * 
     * @param notificationId El identificador de la notificación a reintentar
     * @return Value Object con la información de la notificación reintentada
     */
    NotificationResponse retry(UUID notificationId, ClientContextCommand clientContext);
}
