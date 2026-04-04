package edu.market.notification.domain.event;

import edu.market.notification.domain.enums.EventType;
import edu.market.notification.domain.model.Notification;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Evento de dominio que representa la programación de una notificación.
 * 
 * Patrones de diseño implementados:
 * - Domain Event: Implementa el patrón de eventos de dominio para notificar cambios en el estado
 * - Factory Method: Utiliza método estático fromNotification para crear instancias
 * - Immutability: Todos los campos son finales para garantizar la integridad del evento
 * - Metadata Carrier: Transporta metadatos como EVENT_TYPE, VERSION y SOURCE
 */
public class NotificationScheduledEvent extends DomainEvent {
    
    private static final EventType EVENT_TYPE = EventType.NOTIFICATION_SCHEDULED;
    private static final int VERSION = 1;
    private static final String SOURCE = "notification-service";
    
    private final UUID notificationId;
    private final UUID recipientId;
    private final String notificationType;
    private final LocalDateTime scheduledTime;
    
    private NotificationScheduledEvent(UUID notificationId, UUID recipientId, String notificationType, 
                                      LocalDateTime scheduledTime, UUID correlationId) {
        super(EVENT_TYPE, correlationId, SOURCE, VERSION);
        this.notificationId = notificationId;
        this.recipientId = recipientId;
        this.notificationType = notificationType;
        this.scheduledTime = scheduledTime;
    }
    
    /**
     * Crea un nuevo evento de notificación programada a partir de una entidad de notificación.
     * 
     * @param notification La notificación programada
     * @param scheduledTime El momento programado para el envío
     * @return El evento de notificación programada
     */
    public static NotificationScheduledEvent fromNotification(Notification notification, LocalDateTime scheduledTime) {
        return new NotificationScheduledEvent(
                notification.getId(),
                notification.getRecipient().userId(),
                notification.getChannels().stream()
                        .map(Enum::name)
                        .collect(Collectors.joining(", ")),
                scheduledTime,
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
    
    public LocalDateTime getScheduledTime() {
        return scheduledTime;
    }
}
