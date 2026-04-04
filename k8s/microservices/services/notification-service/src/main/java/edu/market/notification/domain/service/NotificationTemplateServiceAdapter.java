package edu.market.notification.domain.service;

import edu.market.notification.domain.enums.NotificationChannelType;
import edu.market.notification.domain.exception.NotificationException;
import edu.market.notification.domain.model.Notification;
import edu.market.notification.domain.model.Template;
import edu.market.notification.domain.port.output.monitoring.LoggingPort;
import edu.market.notification.domain.port.output.service.NotificationTemplateServicePort;
import edu.market.notification.domain.vo.NotificationContentVO;

// MessageFormat ya no es necesario porque usamos los métodos de logging con formato
import java.util.Set;

/**
 * Servicio de dominio que coordina operaciones entre las entidades Notification y Template.
 * Este servicio existe únicamente para manejar la lógica de negocio que requiere
 * la interacción entre estos dos agregados y que no puede ser encapsulada
 * directamente en ninguno de ellos.
 * 
 * Patrones de diseño implementados:
 * - Mediator: Actúa como mediador entre las entidades Notification y Template
 * - Adapter: Implementa la interfaz NotificationTemplateServicePort (Ports and Adapters)
 * - Dependency Injection: Recibe LoggingPort por constructor
 * - Domain Service: Encapsula lógica de negocio que coordina entre entidades
 * - Strategy: Define diferentes algoritmos para verificar compatibilidad y aplicar plantillas
 */
public class NotificationTemplateServiceAdapter implements NotificationTemplateServicePort {
    
    private final LoggingPort log;
    
    public NotificationTemplateServiceAdapter(LoggingPort log) {
        this.log = log;
    }
    
    /**
     * Aplica una plantilla a una notificación, verificando compatibilidad y renderizando el contenido.
     * 
     * @param notification La notificación a la que se aplicará la plantilla
     * @param template La plantilla a aplicar
     * @return La notificación con el contenido de la plantilla aplicado
     * @throws NotificationException Si la plantilla no es compatible con el canal de la notificación
     */
    @Override
    public Notification applyTemplate(Notification notification, Template template) {
        
        log.info("applyTemplate - Init");        
        
        // Verificar que la plantilla esté activa
        log.debug("applyTemplate - Applying template %s to notification %s and verifying if it's active: %s", template.getCode(), notification.getId(), template.isActive());
        if (!template.isActive()) {
            log.warn("applyTemplate - Attempt to apply inactive template");
            throw new NotificationException("Cannot apply an inactive template");
        }
        
        // Verificar compatibilidad entre la plantilla y los canales de notificación
        Set<NotificationChannelType> channels = notification.getChannels();

        log.debug("applyTemplate - Verifying channel compatibility %s with template %s", channels, template.getCode());
        if (!template.supportsChannel(channels)) {
            log.warn("applyTemplate - Template %s does not support channel %s", 
                    template.getCode(), channels);
            throw new NotificationException(
                    String.format("Template %s does not support channel %s", template.getCode(), channels));
        }
        
        // Renderizar el contenido de la plantilla con parámetros
        log.debug("applyTemplate - Rendering template content");
        String renderedContent = template.getContent();
        String renderedSubject = template.getSubject();
        
        // Crear una nueva notificación con el contenido renderizado
        log.debug("applyTemplate - Creating new notification with rendered content");
        Notification result = new Notification.Builder()
                .withId(notification.getId())
                .withSubject(renderedSubject)
                .withContent(new NotificationContentVO(renderedContent, null))
                .withRecipient(notification.getRecipient())
                .withChannels(notification.getChannels())
                .withPriority(notification.getPriority())
                .withStatus(notification.getStatus())
                .withSourceService(notification.getSourceService())
                .withTemplateId(template.getId())
                .withRequesterId(notification.getRequesterId())
                .withCreatedAt(notification.getCreatedAt())                
                .withScheduledFor(notification.getScheduledFor())
                .build();
                
        log.info("applyTemplate - End");
        return result;
    }
    
    /**
     * Verifica si una plantilla es compatible con una notificación
     * 
     * @param notification La notificación a verificar
     * @param template La plantilla a verificar
     * @return true si la plantilla es compatible con la notificación
     */
    @Override
    public boolean isTemplateCompatibleWithNotification(Notification notification, Template template) {
        
        log.info("isTemplateCompatibleWithNotification - Init");
        
        // Verificar que la plantilla esté activa
        log.debug("isTemplateCompatibleWithNotification - Verifying if template %s is active: %s", template.getCode(), template.isActive());
        if (!template.isActive()) {            
            log.info("isTemplateCompatibleWithNotification - End");
            return false;
        }
        
        // Verificar que la plantilla soporte los canales de notificación
        boolean isCompatible = template.supportsChannel(notification.getChannels());
        log.debug("isTemplateCompatibleWithNotification - Channel compatibility %s: %s", notification.getChannels(), isCompatible);
        
        log.info("isTemplateCompatibleWithNotification - End");
        return isCompatible;
    }
}
