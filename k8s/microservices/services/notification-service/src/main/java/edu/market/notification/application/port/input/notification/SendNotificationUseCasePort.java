package edu.market.notification.application.port.input.notification;

import edu.market.notification.application.dto.request.ClientContextCommand;
import edu.market.notification.application.dto.request.NotificationCommand;
import edu.market.notification.application.dto.response.NotificationResponse;

/**
 * Puerto de caso de uso para enviar notificaciones.
 * 
 * Este caso de uso permite enviar notificaciones a través de diferentes canales
 * como email, SMS o notificaciones push, según la configuración especificada.
 */
public interface SendNotificationUseCasePort {
    
    /**
     * Envía una notificación al destinatario especificado.
     * 
     * @param command Value Object con los datos de la notificación a enviar
     * @return Value Object con la información de la notificación enviada
     */
    NotificationResponse send(NotificationCommand command, ClientContextCommand clientContext);
}
