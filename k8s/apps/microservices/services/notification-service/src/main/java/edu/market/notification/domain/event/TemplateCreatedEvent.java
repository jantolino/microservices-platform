package edu.market.notification.domain.event;

import edu.market.notification.domain.enums.EventType;
import edu.market.notification.domain.enums.NotificationChannelType;
import edu.market.notification.domain.model.Template;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Evento de dominio que representa la creación de una nueva plantilla de notificación.
 * 
 * Patrones de diseño implementados:
 * - Domain Event: Implementa el patrón de eventos de dominio para notificar cambios en el estado
 * - Factory Method: Utiliza método estático fromTemplate para crear instancias
 * - Immutability: Todos los campos son finales para garantizar la integridad del evento
 * - Metadata Carrier: Transporta metadatos como EVENT_TYPE, VERSION y SOURCE
 */
public class TemplateCreatedEvent extends DomainEvent {
    
    private static final EventType EVENT_TYPE = EventType.TEMPLATE_CREATED;
    private static final int VERSION = 1;
    private static final String SOURCE = "notification-service";
    
    private final UUID templateId;
    private final String templateCode;
    private final String templateName;
    private final Set<NotificationChannelType> channels;
    private final String language;
    private final int templateVersion;
    private final UUID createdBy;
    
    private TemplateCreatedEvent(UUID templateId, String templateCode, String templateName, 
                                Set<NotificationChannelType> channels, String language, int templateVersion, 
                                UUID createdBy, UUID correlationId) {
        super(EVENT_TYPE, correlationId, SOURCE, VERSION);
        this.templateId = templateId;
        this.templateCode = templateCode;
        this.templateName = templateName;
        this.channels = new HashSet<>(channels);
        this.language = language;
        this.templateVersion = templateVersion;
        this.createdBy = createdBy;
    }
    
    /**
     * Crea un nuevo evento de creación de plantilla a partir de una entidad de plantilla.
     * 
     * @param template La plantilla creada
     * @param createdBy ID del usuario que creó la plantilla
     * @return El evento de creación de plantilla
     */
    public static TemplateCreatedEvent fromTemplate(Template template, UUID createdBy) {
        return new TemplateCreatedEvent(
                template.getId(),
                template.getCode(),
                template.getName(),
                template.getSupportedChannels(),
                template.getLanguage(),
                template.getVersion(),
                createdBy,
                template.getId() // Usando el ID de la plantilla como correlationId
        );
    }
    
    public UUID getTemplateId() {
        return templateId;
    }
    
    public String getTemplateCode() {
        return templateCode;
    }
    
    public String getTemplateName() {
        return templateName;
    }
    
    public Set<NotificationChannelType> getChannels() {
        return new HashSet<>(channels);
    }
    
    public String getLanguage() {
        return language;
    }
    
    public int getTemplateVersion() {
        return templateVersion;
    }
    
    public UUID getCreatedBy() {
        return createdBy;
    }
}
