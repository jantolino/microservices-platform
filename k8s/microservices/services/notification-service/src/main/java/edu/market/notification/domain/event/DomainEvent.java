package edu.market.notification.domain.event;

import java.time.LocalDateTime;
import java.util.UUID;

import edu.market.notification.domain.enums.EventType;

/**
 * Clase base para todos los eventos de dominio.
 * Implementa el patrón Event Sourcing para la comunicación entre servicios.
 */
public abstract class DomainEvent {
    
    private final UUID eventId;
    private final EventType eventType;
    private final LocalDateTime occurredOn;
    private final UUID correlationId;
    private final String source;
    private final int version;

    protected DomainEvent(EventType eventType, UUID correlationId, String source, int version) {
        this.eventId = UUID.randomUUID();
        this.eventType = eventType;
        this.occurredOn = LocalDateTime.now();
        this.correlationId = correlationId;
        this.source = source;
        this.version = version;
    }

    public UUID getEventId() {
        return eventId;
    }

    public EventType getEventType() {
        return eventType;
    }

    public LocalDateTime getOccurredOn() {
        return occurredOn;
    }

    public UUID getCorrelationId() {
        return correlationId;
    }

    public String getSource() {
        return source;
    }

    public int getVersion() {
        return version;
    }
}
