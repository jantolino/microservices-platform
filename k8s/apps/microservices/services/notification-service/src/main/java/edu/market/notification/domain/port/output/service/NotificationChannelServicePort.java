package edu.market.notification.domain.port.output.service;

import edu.market.notification.domain.model.Notification;
import edu.market.notification.domain.model.DeliveryAttempt;

/**
 * Puerto de salida para el envío de notificaciones a través de diferentes canales.
 * Implementa el patrón Bulkhead para aislar los diferentes canales de notificación.
 */
public interface NotificationChannelServicePort {
    
    /**
     * Envía una notificación a través de un canal específico
     * @param notification Notificación a enviar
     * @return Resultado del intento de entrega
     */
    DeliveryAttempt send(Notification notification);
    
    /**
     * Verifica si el canal está disponible
     * @return true si el canal está disponible
     */
    boolean isAvailable();
    
    /**
     * Obtiene el nombre del canal
     * @return Nombre del canal
     */
    String getChannelName();
}
