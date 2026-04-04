package edu.market.notification.domain.model;

import edu.market.notification.domain.event.DomainEvent;
import edu.market.notification.domain.event.NotificationFailedEvent;
import edu.market.notification.domain.event.NotificationSentEvent;

import java.time.Duration;

/**
 * Resultado de un intento de entrega de notificación
 * Encapsula el resultado y los datos necesarios para el caso de uso
 */
public class DeliveryResult {
    
    private final boolean successful;
    private final DomainEvent event;
    private final NotificationAudit audit;
    private final Duration deliveryTime;
    private final String errorMessage;
    
    /**
     * Constructor para envío exitoso
     * @param successful siempre true para este constructor
     * @param event evento de notificación enviada
     * @param audit entrada de auditoría
     * @param deliveryTime tiempo de entrega
     */
    public DeliveryResult(boolean successful, NotificationSentEvent event, NotificationAudit audit, Duration deliveryTime) {
        this.successful = successful;
        this.event = event;
        this.audit = audit;
        this.deliveryTime = deliveryTime;
        this.errorMessage = null;
    }
    
    /**
     * Constructor para envío fallido
     * @param successful siempre false para este constructor
     * @param event evento de notificación fallida
     * @param audit entrada de auditoría
     * @param errorMessage mensaje de error
     */
    public DeliveryResult(boolean successful, NotificationFailedEvent event, NotificationAudit audit, String errorMessage) {
        this.successful = successful;
        this.event = event;
        this.audit = audit;
        this.deliveryTime = null;
        this.errorMessage = errorMessage;
    }
    
    public boolean isSuccessful() {
        return successful;
    }
    
    /**
     * Obtiene el evento asociado al resultado
     * @return NotificationEvent (puede ser NotificationSentEvent o NotificationFailedEvent)
     */
    public DomainEvent getEvent() {
        return event;
    }
    
    /**
     * Obtiene el evento de envío exitoso
     * @return evento de envío exitoso o null si falló
     */
    public NotificationSentEvent getSentEvent() {
        return successful && event instanceof NotificationSentEvent ? 
                (NotificationSentEvent) event : null;
    }
    
    /**
     * Obtiene el evento de envío fallido
     * @return evento de envío fallido o null si fue exitoso
     */
    public NotificationFailedEvent getFailedEvent() {
        return !successful && event instanceof NotificationFailedEvent ? 
                (NotificationFailedEvent) event : null;
    }
    
    public NotificationAudit getAudit() {
        return audit;
    }
    
    public Duration getDeliveryTime() {
        return deliveryTime;
    }
    
    public String getErrorMessage() {
        return errorMessage;
    }
}
