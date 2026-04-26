package edu.market.notification.domain.event;

import edu.market.notification.domain.enums.EventType;
import edu.market.notification.domain.enums.NotificationChannelType;
import edu.market.notification.domain.model.Notification;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Evento de dominio que representa el envío exitoso de una notificación.
 * 
 * Patrones de diseño implementados:
 * - Domain Event: Implementa el patrón de eventos de dominio para notificar cambios en el estado
 * - Event Sourcing: Permite reconstruir el estado del sistema a partir de la secuencia de eventos
 * - Factory Method: Utiliza método estático fromNotification para crear instancias
 * - Value Object: Todos los campos son finales para garantizar la integridad del evento
 * - Metadata Carrier: Transporta metadatos como EVENT_TYPE, VERSION y SOURCE
 */
public class NotificationSentEvent extends DomainEvent {
    
    private static final EventType EVENT_TYPE = EventType.NOTIFICATION_SENT;
    private static final int VERSION = 1;
    private static final String SOURCE = "notification-service";

    private final UUID notificationId;
    private final UUID userId;
    private final Set<NotificationChannelType> channels;
    private final LocalDateTime sentAt;
    private final String deliveryId;

    public NotificationSentEvent(UUID notificationId, UUID userId, Set<NotificationChannelType> channels,
                               LocalDateTime sentAt, String deliveryId, UUID correlationId) {
        super(EVENT_TYPE, correlationId, SOURCE, VERSION);
        this.notificationId = notificationId;
        this.userId = userId;
        this.channels = channels != null ? new HashSet<>(channels) : new HashSet<>();
        this.sentAt = sentAt;
        this.deliveryId = deliveryId;
    }
    
    /**
     * Crea un nuevo evento de notificación enviada a partir de una entidad de notificación.
     * 
     * @param notification La notificación enviada
     * @return El evento de notificación enviada
     */
    public static NotificationSentEvent fromNotification(Notification notification) {
        return new NotificationSentEvent(
                notification.getId(),
                notification.getRecipient().userId(),
                notification.getChannels(),
                LocalDateTime.now(),
                UUID.randomUUID().toString(), // Generamos un ID de entrega único
                notification.getId() // Usando el ID de notificación como correlationId
        );
    }

    public UUID getNotificationId() {
        return notificationId;
    }

    public UUID getUserId() {
        return userId;
    }

    public Set<NotificationChannelType> getChannels() {
        return new HashSet<>(channels);
    }

    public LocalDateTime getSentAt() {
        return sentAt;
    }

    public String getDeliveryId() {
        return deliveryId;
    }
}
