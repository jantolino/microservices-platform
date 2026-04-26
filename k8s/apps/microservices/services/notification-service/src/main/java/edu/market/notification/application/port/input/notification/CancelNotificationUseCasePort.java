package edu.market.notification.application.port.input.notification;

import java.util.UUID;

import edu.market.notification.application.dto.request.ClientContextCommand;
import edu.market.notification.application.dto.response.NotificationResponse;

/**
 * Puerto de caso de uso para cancelar notificaciones programadas.
 * 
 * Este caso de uso permite cancelar notificaciones que han sido programadas
 * para envío futuro pero que aún no han sido enviadas.
 */
public interface CancelNotificationUseCasePort {
    
    /**
     * Cancela una notificación programada.
     * 
     * @param notificationId El identificador de la notificación a cancelar
     * @return Value Object con la información de la notificación cancelada
     */
    NotificationResponse cancel(UUID notificationId, ClientContextCommand clientContext);
}
