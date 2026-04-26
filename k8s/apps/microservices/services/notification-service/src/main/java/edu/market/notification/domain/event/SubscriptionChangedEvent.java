package edu.market.notification.domain.event;

import edu.market.notification.domain.enums.EventType;
import edu.market.notification.domain.enums.NotificationChannelType;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Evento de dominio que representa un cambio en las preferencias de suscripción de un usuario.
 * 
 * Patrones de diseño implementados:
 * - Domain Event: Implementa el patrón de eventos de dominio para notificar cambios en el estado
 * - Event Sourcing: Permite reconstruir el estado del sistema a partir de la secuencia de eventos
 * - Immutability: Todos los campos son finales para garantizar la integridad del evento
 * - Metadata Carrier: Transporta metadatos como EVENT_TYPE, VERSION y SOURCE
 * - Defensive Copying: Utiliza copias defensivas en el constructor y getters para colecciones
 * - Value Object: Utiliza objetos inmutables para representar las preferencias del usuario
 */
public class SubscriptionChangedEvent extends DomainEvent {
    
    private static final EventType EVENT_TYPE = EventType.SUSBCRIPTION_CHANGED;
    private static final int VERSION = 1;
    private static final String SOURCE = "notification-service";

    private final UUID userId;
    private final Map<String, Set<NotificationChannelType>> eventPreferences;
    private final boolean globalOptOut;
    private final String changeType;

    public SubscriptionChangedEvent(UUID userId, Map<String, Set<NotificationChannelType>> eventPreferences,
                                  boolean globalOptOut, String changeType, UUID correlationId) {
        super(EVENT_TYPE, correlationId, SOURCE, VERSION);
        this.userId = userId;
        this.eventPreferences = new HashMap<>(eventPreferences);
        this.globalOptOut = globalOptOut;
        this.changeType = changeType;
    }

    public UUID getUserId() {
        return userId;
    }

    public Map<String, Set<NotificationChannelType>> getEventPreferences() {
        return new HashMap<>(eventPreferences);
    }

    public boolean isGlobalOptOut() {
        return globalOptOut;
    }

    public String getChangeType() {
        return changeType;
    }
}
