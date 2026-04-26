package edu.market.notification.application.port.input.notification;

import java.util.UUID;

import edu.market.notification.application.dto.request.ClientContextCommand;
import edu.market.notification.application.dto.response.NotificationStatusResponse;

/**
 * Puerto de caso de uso para verificar el estado de una notificación.
 * 
 * Este caso de uso permite verificar el estado actual de una notificación específica,
 * como si ha sido enviada, ha fallado o aún está pendiente.
 */
public interface CheckNotificationStatusUseCasePort {
    
    /**
     * Verifica el estado actual de una notificación específica.
     * 
     * @param notificationId El identificador de la notificación a verificar
     * @return Value Object con la información del estado de la notificación
     */
    NotificationStatusResponse check(UUID notificationId, ClientContextCommand clientContext);
}
