package edu.market.notification.domain.event;

import edu.market.notification.domain.enums.EventType;
import edu.market.notification.domain.model.Notification;

import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Evento de dominio que representa la cancelación de una notificación programada.
 * 
 * Patrones de diseño implementados:
 * - Domain Event: Implementa el patrón de eventos de dominio para notificar cambios en el estado
 * - Factory Method: Utiliza método estático fromNotification para crear instancias
 * - Immutability: Todos los campos son finales para garantizar la integridad del evento
 * - Metadata Carrier: Transporta metadatos como EVENT_TYPE, VERSION y SOURCE
 */
public class NotificationCanceledEvent extends DomainEvent {
    
    private static final EventType EVENT_TYPE = EventType.NOTIFICATION_CANCELED;    
    private static final String SOURCE = "notification-service";
    private static final int VERSION = 1;
    
    private final UUID notificationId;
    private final UUID recipientId;
    private final String notificationType;
    private final String reason;
        
    private NotificationCanceledEvent(UUID notificationId, UUID recipientId, String notificationType, 
                                     String reason, UUID correlationId) {
        super(EVENT_TYPE, correlationId, SOURCE, VERSION);
        this.notificationId = notificationId;
        this.recipientId = recipientId;
        this.notificationType = notificationType;
        this.reason = reason;
    }
    
    /**
     * Crea un nuevo evento de notificación cancelada a partir de una entidad de notificación.
     * 
     * @param notification La notificación cancelada
     * @param reason Motivo de la cancelación
     * @return El evento de notificación cancelada
     */
    public static NotificationCanceledEvent fromNotification(Notification notification, String reason) {
        return new NotificationCanceledEvent(
                notification.getId(),
                notification.getRecipient().userId(),
                notification.getChannels().stream()
                        .map(Enum::name)
                        .collect(Collectors.joining(", ")),
                reason,
                notification.getId() // Usando el ID de notificación como correlationId
        );
    }
    
    public UUID getNotificationId() {
        return notificationId;
    }
    
    public UUID getRecipientId() {
        return recipientId;
    }
    
    public String getNotificationType() {
        return notificationType;
    }
    
    public String getReason() {
        return reason;
    }    
}
