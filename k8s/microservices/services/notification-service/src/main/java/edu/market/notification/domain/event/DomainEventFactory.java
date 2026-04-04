package edu.market.notification.domain.event;

import edu.market.notification.domain.model.Notification;
import edu.market.notification.domain.model.Template;
import edu.market.notification.domain.model.UserPreference;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

/**
 * Fábrica para la creación de eventos de dominio relacionados con notificaciones.
 * 
 * Patrones de diseño implementados:
 * - Factory: Centraliza la creación de todos los eventos de dominio relacionados con notificaciones
 * - Facade: Proporciona una interfaz unificada para la creación de eventos de diferentes tipos
 * - Static Utility: Utiliza métodos estáticos para evitar la necesidad de instanciar la clase
 * - Delegation: Delega la creación real de los eventos a los métodos factory de cada clase de evento
 * 
 * Esta clase centraliza la creación de eventos de dominio, siguiendo el principio
 * de que el dominio debe ser responsable de la creación de eventos, mientras que
 * los casos de uso orquestan su ejecución. Esto facilita la trazabilidad y mantiene
 * la coherencia en la creación de eventos a lo largo de toda la aplicación.
 */
public class DomainEventFactory {
    
    /**
     * Crea un evento de notificación enviada.
     * 
     * @param notification La notificación enviada
     * @return El evento de notificación enviada
     */
    public static NotificationSentEvent createNotificationSentEvent(Notification notification) {
        return NotificationSentEvent.fromNotification(notification);
    }
    
    /**
     * Crea un evento de notificación programada.
     * 
     * @param notification La notificación programada
     * @param scheduledTime El momento programado para el envío
     * @return El evento de notificación programada
     */
    public static NotificationScheduledEvent createNotificationScheduledEvent(Notification notification, LocalDateTime scheduledTime) {
        return NotificationScheduledEvent.fromNotification(notification, scheduledTime);
    }
    
    /**
     * Crea un evento de notificación cancelada.
     * 
     * @param notification La notificación cancelada
     * @param reason Motivo de la cancelación
     * @return El evento de notificación cancelada
     */
    public static NotificationCanceledEvent createNotificationCanceledEvent(Notification notification, String reason) {
        return NotificationCanceledEvent.fromNotification(notification, reason);
    }    
    
    
    /**
     * Crea un evento de notificación fallida.
     * 
     * @param notification La notificación fallida
     * @param errorMessage Mensaje de error
     * @return El evento de notificación fallida
     */
    public static NotificationFailedEvent createNotificationFailedEvent(Notification notification, String errorMessage) {
        return NotificationFailedEvent.fromNotification(notification, errorMessage);
    }
    
    /**
     * Crea un evento de plantilla creada.
     * 
     * @param template La plantilla creada
     * @param createdBy ID del usuario que creó la plantilla
     * @return El evento de plantilla creada
     */
    public static TemplateCreatedEvent createTemplateCreatedEvent(Template template, UUID createdBy) {
        return TemplateCreatedEvent.fromTemplate(template, createdBy);
    }
    
    /**
     * Crea un evento de plantilla actualizada.
     * 
     * @param template La plantilla actualizada
     * @param updatedBy ID del usuario que actualizó la plantilla
     * @param updateType Tipo de actualización (contenido, metadatos, estado)
     * @return El evento de plantilla actualizada
     */
    public static TemplateUpdatedEvent createTemplateUpdatedEvent(Template template, UUID updatedBy, String updateType) {
        return TemplateUpdatedEvent.fromTemplate(template, updatedBy, updateType);
    }
    
    /**
     * Crea un evento de preferencias de usuario actualizadas.
     * 
     * @param preference Las preferencias actualizadas
     * @return El evento de preferencias actualizadas
     */
    public static UserPreferenceUpdatedEvent createUserPreferenceUpdatedEvent(UserPreference preference) {
        return UserPreferenceUpdatedEvent.fromUserPreference(preference);
    }
    
    /**
     * Crea un evento de deshabilitación global de notificaciones para un usuario.
     * 
     * @param userId ID del usuario
     * @param reason Motivo de la deshabilitación
     * @return El evento de notificaciones deshabilitadas
     */
    public static NotificationsDisabledEvent createGlobalDisableEvent(UUID userId, String reason) {
        return NotificationsDisabledEvent.globalDisable(userId, reason);
    }
    
    /**
     * Crea un evento de deshabilitación de notificaciones específicas para un usuario.
     * 
     * @param userId ID del usuario
     * @param disabledEventTypes Tipos de eventos deshabilitados
     * @param reason Motivo de la deshabilitación
     * @return El evento de notificaciones deshabilitadas
     */
    public static NotificationsDisabledEvent createSpecificEventsDisableEvent(UUID userId, Set<String> disabledEventTypes, String reason) {
        return NotificationsDisabledEvent.specificEventsDisable(userId, disabledEventTypes, reason);
    }
    
    /**
     * Crea un evento de notificación creada.
     * 
     * @param notification La notificación creada
     * @return El evento de notificación creada
     */
    public static NotificationCreatedEvent createNotificationCreatedEvent(Notification notification) {
        return NotificationCreatedEvent.fromNotification(notification);
    }
}
