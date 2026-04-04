package edu.market.notification.domain.event;

import java.util.Set;
import java.util.UUID;

import edu.market.notification.domain.enums.EventType;

/**
 * Evento de dominio que representa la deshabilitación de notificaciones para un usuario.
 * 
 * Patrones de diseño implementados:
 * - Domain Event: Implementa el patrón de eventos de dominio para notificar cambios en el estado
 * - Factory Method: Utiliza métodos estáticos globalDisable y specificEventsDisable para crear instancias
 * - Immutability: Todos los campos son finales para garantizar la integridad del evento
 * - Metadata Carrier: Transporta metadatos como EVENT_TYPE, VERSION y SOURCE 
 * - Value Object: Utiliza objetos inmutables para representar las preferencias del usuario
 */
public class NotificationsDisabledEvent extends DomainEvent {
    
    private static final EventType EVENT_TYPE = EventType.NOTIFICATION_DISABLED;
    private static final int VERSION = 1;
    private static final String SOURCE = "notification-service";
    
    private final UUID userId;
    private final boolean globalDisable;
    private final Set<String> disabledEventTypes;
    private final String reason;
    
    private NotificationsDisabledEvent(UUID userId, boolean globalDisable, Set<String> disabledEventTypes, String reason, UUID correlationId) {
        super(EVENT_TYPE, correlationId, SOURCE, VERSION);
        this.userId = userId;
        this.globalDisable = globalDisable;
        this.disabledEventTypes = disabledEventTypes;
        this.reason = reason;
    }
    
    /**
     * Crea un nuevo evento de deshabilitación global de notificaciones para un usuario.
     * 
     * @param userId ID del usuario
     * @param reason Motivo de la deshabilitación
     * @return El evento de notificaciones deshabilitadas
     */
    public static NotificationsDisabledEvent globalDisable(UUID userId, String reason) {
        return new NotificationsDisabledEvent(userId, true, null, reason, userId);
    }
    
    /**
     * Crea un nuevo evento de deshabilitación de notificaciones específicas para un usuario.
     * 
     * @param userId ID del usuario
     * @param disabledEventTypes Tipos de eventos deshabilitados
     * @param reason Motivo de la deshabilitación
     * @return El evento de notificaciones deshabilitadas
     */
    public static NotificationsDisabledEvent specificEventsDisable(UUID userId, Set<String> disabledEventTypes, String reason) {
        return new NotificationsDisabledEvent(userId, false, disabledEventTypes, reason, userId);
    }
    
    public UUID getUserId() {
        return userId;
    }
    
    public boolean isGlobalDisable() {
        return globalDisable;
    }
    
    public Set<String> getDisabledEventTypes() {
        return disabledEventTypes;
    }
    
    public String getReason() {
        return reason;
    }
}
