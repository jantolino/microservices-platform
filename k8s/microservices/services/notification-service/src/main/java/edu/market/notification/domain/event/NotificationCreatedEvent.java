package edu.market.notification.domain.event;

import edu.market.notification.domain.enums.EventType;
import edu.market.notification.domain.enums.NotificationChannelType;
import edu.market.notification.domain.enums.NotificationPriorityType;
import edu.market.notification.domain.enums.SourceServiceType;
import edu.market.notification.domain.model.Notification;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Evento de dominio que representa la creación de una notificación.
 * 
 * Patrones de diseño implementados:
 * - Domain Event: Implementa el patrón de eventos de dominio para notificar cambios en el estado
 * - Event Sourcing: Permite reconstruir el estado del sistema a partir de la secuencia de eventos
 * - Transactional Outbox: Garantiza la consistencia entre la base de datos y los mensajes publicados
 * - Factory Method: Utiliza método estático fromNotification para crear instancias
 * - Immutability: Todos los campos son finales para garantizar la integridad del evento
 * - Metadata Carrier: Transporta metadatos como EVENT_TYPE, VERSION y SOURCE
 */
public class NotificationCreatedEvent extends DomainEvent {
    
    private static final EventType EVENT_TYPE = EventType.NOTIFICATION_CREATED;
    private static final int VERSION = 1;
    private static final String SOURCE = "notification-service";

    private final UUID notificationId;
    private final UUID userId;
    private final String subject;
    private final Set<NotificationChannelType> channels;
    private final NotificationPriorityType priority;
    private final UUID templateId;
    private final UUID requesterId;
    private final SourceServiceType sourceService;

    private NotificationCreatedEvent(UUID notificationId, UUID userId, String subject, 
                                   Set<NotificationChannelType> channels, NotificationPriorityType priority,
                                   UUID templateId, UUID correlationId, UUID requesterId, SourceServiceType sourceService) {
        
        super(EVENT_TYPE, correlationId, SOURCE, VERSION);
        this.notificationId = notificationId;
        this.userId = userId;
        this.subject = subject;
        this.channels = channels != null ? new HashSet<>(channels) : new HashSet<>();
        this.priority = priority;
        this.templateId = templateId;
        this.requesterId = requesterId;
        this.sourceService = sourceService != null ? sourceService : SourceServiceType.UNKNOWN;
    }
    
    /**
     * Crea un nuevo evento de notificación creada a partir de una entidad de notificación.
     * 
     * @param notification La notificación creada
     * @return El evento de notificación creada
     */
    public static NotificationCreatedEvent fromNotification(Notification notification) {
        return new NotificationCreatedEvent(
                notification.getId(),
                notification.getRecipient().userId(),
                notification.getSubject(),
                notification.getChannels(),
                notification.getPriority(),
                notification.getTemplateId(),
                notification.getId(), // Usando el ID de notificación como correlationId
                notification.getRequesterId(),
                notification.getSourceService()
        );
    }

    public UUID getNotificationId() {
        return notificationId;
    }

    public UUID getUserId() {
        return userId;
    }

    public String getSubject() {
        return subject;
    }

    public Set<NotificationChannelType> getChannels() {
        return new HashSet<>(channels);
    }

    public NotificationPriorityType getPriority() {
        return priority;
    }

    public UUID getTemplateId() {
        return templateId;
    }
    
    public UUID getRequesterId() {
        return requesterId;
    }
    
    public SourceServiceType getSourceService() {
        return sourceService;
    }
}
