package edu.market.notification.domain.event;

import edu.market.notification.domain.enums.EventType;
import edu.market.notification.domain.enums.NotificationChannelType;
import edu.market.notification.domain.model.Notification;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Evento de dominio que representa el fallo en el envío de una notificación.
 * 
 * Patrones de diseño implementados:
 * - Domain Event: Implementa el patrón de eventos de dominio para notificar cambios en el estado
 * - Dead Letter Queue: Permite gestionar mensajes que no pueden ser procesados correctamente
 * - Circuit Breaker: Ayuda a detectar fallos recurrentes mediante el campo retryable
 * - Factory Method: Utiliza método estático fromNotification para crear instancias
 * - Immutability: Todos los campos son finales para garantizar la integridad del evento
 * - Metadata Carrier: Transporta metadatos como EVENT_TYPE, VERSION y SOURCE
 */
public class NotificationFailedEvent extends DomainEvent {
    
    private static final EventType EVENT_TYPE = EventType.NOTIFICATION_FAILED;
    private static final int VERSION = 1;
    private static final String SOURCE = "notification-service";

    private final UUID notificationId;
    private final UUID userId;
    private final Set<NotificationChannelType> channels;
    private final LocalDateTime failedAt;
    private final String errorMessage;
    private final boolean retryable;

    public NotificationFailedEvent(UUID notificationId, UUID userId, Set<NotificationChannelType> channels,
                                 LocalDateTime failedAt, String errorMessage, boolean retryable, UUID correlationId) {
        super(EVENT_TYPE, correlationId, SOURCE, VERSION);
        this.notificationId = notificationId;
        this.userId = userId;
        this.channels = channels != null ? new HashSet<>(channels) : new HashSet<>();
        this.failedAt = failedAt;
        this.errorMessage = errorMessage;
        this.retryable = retryable;
    }
    
    /**
     * Crea un nuevo evento de notificación fallida a partir de una entidad de notificación.
     * 
     * @param notification La notificación fallida
     * @param errorMessage Mensaje de error
     * @return El evento de notificación fallida
     */
    public static NotificationFailedEvent fromNotification(Notification notification, String errorMessage) {
        // Asumimos un máximo de 3 reintentos como valor por defecto
        // En un sistema real, esto podría venir de una configuración
        int maxRetries = 3;
        return new NotificationFailedEvent(
                notification.getId(),
                notification.getRecipient().userId(),
                notification.getChannels(),
                LocalDateTime.now(),
                errorMessage,
                notification.canRetry(maxRetries), // Usamos el método canRetry para determinar si es reintentatble
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

    public LocalDateTime getFailedAt() {
        return failedAt;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public boolean isRetryable() {
        return retryable;
    }
}
