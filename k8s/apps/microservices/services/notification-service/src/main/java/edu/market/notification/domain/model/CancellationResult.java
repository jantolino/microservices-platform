package edu.market.notification.domain.model;

import edu.market.notification.domain.exception.NotificationException;

/**
 * Resultado de un intento de cancelación de notificación
 * Encapsula el resultado y los datos necesarios para el caso de uso
 */
public class CancellationResult {
    
    private final boolean successful;
    private final Notification notification;
    private final NotificationAudit audit;
    
    public CancellationResult(boolean successful, Notification notification, NotificationAudit audit) {
        this.successful = successful;
        this.notification = notification;
        this.audit = audit;
        
        validate();
    }
    
    private void validate() {
        if (notification == null) {
            throw new NotificationException("notification required");
        }
    }
    
    public boolean isSuccessful() {
        return successful;
    }
    
    public Notification getNotification() {
        return notification;
    }
    
    public NotificationAudit getAudit() {
        return audit;
    }
}
