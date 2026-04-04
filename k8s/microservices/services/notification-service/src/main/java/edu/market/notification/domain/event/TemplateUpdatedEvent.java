package edu.market.notification.domain.event;

import edu.market.notification.domain.enums.EventType;
import edu.market.notification.domain.enums.NotificationChannelType;
import edu.market.notification.domain.model.Template;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Evento de dominio que representa la actualización de una plantilla de notificación.
 * 
 * Patrones de diseño implementados:
 * - Domain Event: Implementa el patrón de eventos de dominio para notificar cambios en el estado
 * - Factory Method: Utiliza método estático fromTemplate para crear instancias
 * - Immutability: Todos los campos son finales para garantizar la integridad del evento
 * - Metadata Carrier: Transporta metadatos como EVENT_TYPE, VERSION y SOURCE
 */
public class TemplateUpdatedEvent extends DomainEvent {
    
    private static final EventType EVENT_TYPE = EventType.TEMPLATE_CHANGED;
    private static final int VERSION = 1;
    private static final String SOURCE = "notification-service";
    
    private final UUID templateId;
    private final String templateCode;
    private final Set<NotificationChannelType> channels;
    private final String language;
    private final UUID updatedBy;
    private final String updateType;
    
    private TemplateUpdatedEvent(UUID templateId, String templateCode, Set<NotificationChannelType> channels, 
                               String language, UUID updatedBy, String updateType, UUID correlationId) {
        super(EVENT_TYPE, correlationId, SOURCE, VERSION);
        this.templateId = templateId;
        this.templateCode = templateCode;
        this.channels = new HashSet<>(channels);
        this.language = language;
        this.updatedBy = updatedBy;
        this.updateType = updateType;
    }
    
    /**
     * Crea un nuevo evento de plantilla actualizada a partir de una entidad de plantilla.
     * 
     * @param template La plantilla actualizada
     * @param updatedBy ID del usuario que actualizó la plantilla
     * @param updateType Tipo de actualización (contenido, metadatos, estado)
     * @return El evento de plantilla actualizada
     */
    public static TemplateUpdatedEvent fromTemplate(Template template, UUID updatedBy, String updateType) {
        return new TemplateUpdatedEvent(
                template.getId(),
                template.getCode(),
                template.getSupportedChannels(),
                template.getLanguage(),
                updatedBy,
                updateType,
                template.getId() // Usando el ID de la plantilla como correlationId
        );
    }
    
    public UUID getTemplateId() {
        return templateId;
    }
    
    public String getTemplateCode() {
        return templateCode;
    }
    
    public Set<NotificationChannelType> getChannels() {
        return new HashSet<>(channels);
    }
    
    public String getLanguage() {
        return language;
    }
    
    public UUID getUpdatedBy() {
        return updatedBy;
    }
    
    public String getUpdateType() {
        return updateType;
    }
}
