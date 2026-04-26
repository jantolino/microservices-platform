package edu.market.notification.domain.event;

import edu.market.notification.domain.enums.EventType;
import edu.market.notification.domain.enums.NotificationChannelType;
import edu.market.notification.domain.model.UserPreference;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Evento de dominio que representa la actualización de preferencias de notificación de un usuario.
 * 
 * Patrones de diseño implementados:
 * - Domain Event: Implementa el patrón de eventos de dominio para notificar cambios en el estado
 * - Event Sourcing: Permite reconstruir el estado del sistema a partir de la secuencia de eventos
 * - Factory Method: Utiliza método estático fromUserPreference para crear instancias
 * - Immutability: Todos los campos son finales para garantizar la integridad del evento
 * - Metadata Carrier: Transporta metadatos como EVENT_TYPE, VERSION y SOURCE
 * - Value Object: Utiliza objetos inmutables para representar las preferencias del usuario
 */
public class UserPreferenceUpdatedEvent extends DomainEvent {
    
    private static final EventType EVENT_TYPE = EventType.USER_PREFERENCE_CHANGED;
    private static final int VERSION = 1;
    private static final String SOURCE = "notification-service";
    
    private final UUID userId;
    private final Set<NotificationChannelType> enabledChannels;
    private final Map<String, Set<NotificationChannelType>> eventPreferences;
    private final boolean globalOptOut;
    
    private UserPreferenceUpdatedEvent(UUID userId, Set<NotificationChannelType> enabledChannels, 
                                     Map<String, Set<NotificationChannelType>> eventPreferences,
                                     boolean globalOptOut, UUID correlationId) {
        super(EVENT_TYPE, correlationId, SOURCE, VERSION);
        this.userId = userId;
        this.enabledChannels = enabledChannels;
        this.eventPreferences = eventPreferences;
        this.globalOptOut = globalOptOut;
    }
    
    /**
     * Crea un nuevo evento de preferencias de usuario actualizadas a partir de una entidad de preferencias.
     * 
     * @param preference Las preferencias actualizadas
     * @return El evento de preferencias actualizadas
     */
    public static UserPreferenceUpdatedEvent fromUserPreference(UserPreference preference) {
        return new UserPreferenceUpdatedEvent(
                preference.getUserId(),
                preference.getEnabledChannels(),
                preference.getEventPreferences(),
                preference.isGlobalOptOut(),
                preference.getUserId() // Usando el ID del usuario como correlationId
        );
    }
    
    public UUID getUserId() {
        return userId;
    }
    
    public Set<NotificationChannelType> getEnabledChannels() {
        return enabledChannels;
    }
    
    public Map<String, Set<NotificationChannelType>> getEventPreferences() {
        return eventPreferences;
    }
    
    public boolean isGlobalOptOut() {
        return globalOptOut;
    }
}
