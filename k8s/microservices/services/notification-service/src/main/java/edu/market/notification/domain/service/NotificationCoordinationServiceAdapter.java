package edu.market.notification.domain.service;

import edu.market.notification.domain.enums.NotificationChannelType;
import edu.market.notification.domain.exception.NotificationException;
import edu.market.notification.domain.model.Notification;
import edu.market.notification.domain.model.Template;
import edu.market.notification.domain.model.UserPreference;
import edu.market.notification.domain.port.output.monitoring.LoggingPort;
import edu.market.notification.domain.port.output.service.NotificationCoordinationServicePort;
import edu.market.notification.domain.vo.NotificationContentVO;

import java.util.Set;

/**
 * Servicio de dominio que coordina operaciones entre múltiples agregados del dominio
 * de notificaciones. Este servicio existe únicamente para manejar la lógica de negocio
 * que requiere la interacción entre múltiples entidades y que no puede ser encapsulada
 * directamente en ninguna de ellas.
 * 
 * Patrones de diseño implementados:
 * - Mediator: Actúa como mediador entre las entidades Notification, Template y UserPreference
 * - Adapter: Implementa la interfaz NotificationCoordinationServicePort (Ports and Adapters)
 * - Dependency Injection: Recibe LoggingPort por constructor
 * - Domain Service: Encapsula lógica de negocio que no pertenece a una sola entidad
 */
public class NotificationCoordinationServiceAdapter implements NotificationCoordinationServicePort {
    
    private final LoggingPort log;
    
    public NotificationCoordinationServiceAdapter(LoggingPort log) {
        this.log = log;
    }
    
    /**
     * Determina si una notificación debe ser enviada basándose en las preferencias del usuario.
     * Esta lógica requiere coordinar entre Notification y UserPreference.
     * 
     * @param notification La notificación a evaluar
     * @param userPreference Las preferencias del usuario
     * @return true si la notificación debe ser enviada según las preferencias del usuario
     */
    @Override
    public boolean shouldSendNotificationBasedOnPreferences(Notification notification, UserPreference userPreference) {
        log.info("shouldSendNotificationBasedOnPreferences - Init");
        
        log.debug("shouldSendNotificationBasedOnPreferences - Evaluating notification %s for user %s", 
                notification.getId(), userPreference.getUserId());
        
        // Si el usuario ha optado por no recibir notificaciones globalmente
        log.debug("shouldSendNotificationBasedOnPreferences - User %s globalOptOut: %s", 
                userPreference.getUserId(), userPreference.isGlobalOptOut());
        if (userPreference.isGlobalOptOut()) {
            log.debug("shouldSendNotificationBasedOnPreferences - User has opted out of notifications globally");
            log.info("shouldSendNotificationBasedOnPreferences - End: false");
            return false;
        }
        
        // Determine the event type based on the notification
        String eventType = determineEventTypeFromNotification(notification);
        log.debug("shouldSendNotificationBasedOnPreferences - Determined event type: %s", eventType);
        
        // Verificar si el usuario está suscrito a este tipo de evento a través de este canal
        boolean shouldSend = userPreference.isSubscribedToEventViaChannel(eventType, notification.getChannels());
        log.debug("shouldSendNotificationBasedOnPreferences - User %s subscribed to event %s via channel %s: %s", 
                userPreference.getUserId(), eventType, notification.getChannels(), shouldSend);
        
        log.info("shouldSendNotificationBasedOnPreferences - End");
        return shouldSend;
    }
    
    /**
     * Determina el tipo de evento basado en la notificación.
     * Este es un método de ayuda para traducir una notificación a un tipo de evento
     * que pueda ser utilizado con las preferencias de usuario.
     * 
     * @param notification La notificación
     * @return El tipo de evento correspondiente
     */
    private String determineEventTypeFromNotification(Notification notification) {
        // En un sistema real, esto podría ser un campo de la notificación
        // o determinarse por alguna lógica más compleja
        if (notification.getTemplateId() != null) {
            return "template." + notification.getTemplateId();
        } else if (notification.getSourceService() != null) {
            return notification.getSourceService().name().toLowerCase() + ".notification";
        } else {
            return "general.notification";
        }
    }
    
    /**
     * Aplica una plantilla a una notificación, verificando compatibilidad y generando
     * el contenido adecuado. Esta lógica requiere coordinar entre Notification y Template.
     * 
     * @param notification La notificación base
     * @param template La plantilla a aplicar
     * @param parameters Parámetros para renderizar la plantilla
     * @return Una nueva notificación con el contenido de la plantilla aplicado
     */
    public Notification applyTemplateToNotification(Notification notification, Template template, Object parameters) {
        log.info("applyTemplateToNotification - Init");
        
        log.debug("applyTemplateToNotification - Applying template %s to notification %s",  template.getCode(), notification.getId());
        
        // Verificar que la plantilla esté activa
        log.debug("applyTemplateToNotification - Verifying if template %s is active: %s", template.getCode(), template.isActive());
        if (!template.isActive()) {
            log.warn("applyTemplateToNotification - Attempt to apply inactive template");
            throw new NotificationException("Cannot apply an inactive template");
        }
        
        // Verificar compatibilidad entre la plantilla y los canales de notificación
        Set<NotificationChannelType> channels = notification.getChannels();
        log.debug("applyTemplateToNotification - Verifying channel compatibility %s with template %s", channels, template.getCode());
        if (!template.supportsChannel(channels)) {
            log.warn("applyTemplateToNotification - Template %s does not support channel %s", template.getCode(), channels);
            throw new NotificationException(String.format("Template %s does not support any of the channels %s", template.getCode(), channels));
        }
        
        // Renderizar el contenido de la plantilla con parámetros
        // En una implementación real, se usaría un motor de plantillas aquí
        log.debug("applyTemplateToNotification - Rendering template content");
        String renderedContent = template.getContent();
        String renderedSubject = template.getSubject();
        
        // Crear una nueva notificación con el contenido renderizado
        log.debug("applyTemplateToNotification - Creating new notification with rendered content");
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
                
        log.info("applyTemplateToNotification - End");
        return result;
    }
    
    /**
     * Determina los canales preferidos para enviar una notificación a un usuario.
     * Esta lógica requiere coordinar entre las preferencias del usuario y el tipo de notificación.
     * 
     * @param notification La notificación a enviar
     * @param userPreference Las preferencias del usuario
     * @return El conjunto de canales preferidos para esta notificación
     */
    @Override
    public Set<NotificationChannelType> determinePreferredChannelsForNotification(
            Notification notification, UserPreference userPreference) {
        
        log.info("determinePreferredChannelsForNotification - Init");        
        
        // Si el usuario ha optado por no recibir notificaciones
        log.debug("determinePreferredChannelsForNotification - User %s globalOptOut: %s", userPreference.getUserId(), userPreference.isGlobalOptOut());
        if (userPreference.isGlobalOptOut()) {
            log.debug("determinePreferredChannelsForNotification - El usuario ha optado por no recibir notificaciones");
            
            log.info("determinePreferredChannelsForNotification - End");
            return Set.of();
        }
        
        // Determine the event type based on the notification
        String eventType = determineEventTypeFromNotification(notification);
        log.debug("determinePreferredChannelsForNotification - Determined event type: %s", eventType);
        
        // Obtener los canales preferidos para el evento
        Set<NotificationChannelType> preferredChannels = userPreference.getPreferredChannelsForEvent(eventType);
        log.debug("determinePreferredChannelsForNotification - Preferred channels for event %s: %s", eventType, preferredChannels);
        
        // Si no hay preferencias específicas, usar los canales de la notificación
        if (preferredChannels.isEmpty() && notification.getChannels() != null && !notification.getChannels().isEmpty()) {            
            log.info("determinePreferredChannelsForNotification - End");
            return notification.getChannels();
        }
        
        log.info("determinePreferredChannelsForNotification - End");
        return preferredChannels;
    }
}
