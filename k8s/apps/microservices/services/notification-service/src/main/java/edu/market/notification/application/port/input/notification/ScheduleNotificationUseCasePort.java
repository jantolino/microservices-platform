package edu.market.notification.application.port.input.notification;

import edu.market.notification.application.dto.request.ClientContextCommand;
import edu.market.notification.application.dto.request.NotificationCommand;
import edu.market.notification.application.dto.response.NotificationResponse;

/**
 * Puerto de caso de uso para programar notificaciones para envío futuro.
 * 
 * Este caso de uso permite programar notificaciones para que sean enviadas
 * en una fecha y hora específica en el futuro.
 */
public interface ScheduleNotificationUseCasePort {
    
    /**
     * Programa una notificación para ser enviada en una fecha y hora específica.
     * 
     * @param command Value Object con los datos de la notificación a programar
     * @return Value Object con la información de la notificación programada
     */
    NotificationResponse schedule(NotificationCommand command, ClientContextCommand clientContext);
}
