package edu.market.notification.domain.model;

import edu.market.notification.domain.enums.SourceServiceType;
import edu.market.notification.domain.exception.EventSubscriptionException;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Modelo de dominio que representa una suscripción a eventos de otros microservicios.
 */
public class EventSubscription {
    
    private final UUID id;
    private String eventType;
    private SourceServiceType sourceService;
    private boolean active;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private EventSubscription(Builder builder) {
        this.id = builder.id != null ? builder.id : UUID.randomUUID();
        this.eventType = builder.eventType;
        this.sourceService = builder.sourceService;
        this.active = builder.active;
        this.createdAt = builder.createdAt != null ? builder.createdAt : LocalDateTime.now();
        this.updatedAt = builder.updatedAt;
        
        validate();
    }
    
    private void validate() {
        if (eventType == null || eventType.isBlank()) {
            throw new EventSubscriptionException("eventType required");
        }
        
        if (sourceService == null) {
            throw new EventSubscriptionException("sourceService required");
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    public UUID getId() {
        return id;
    }

    public String getEventType() {
        return eventType;
    }

    public SourceServiceType getSourceService() {
        return sourceService;
    }

    public boolean isActive() {
        return active;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setActive(boolean active) {
        this.active = active;
        this.updatedAt = LocalDateTime.now();
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
        this.updatedAt = LocalDateTime.now();
    }

    public void setSourceService(SourceServiceType sourceService) {
        this.sourceService = sourceService;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Builder para la creación de instancias de EventSubscription.
     */
    public static class Builder {
        private UUID id;
        private String eventType;
        private SourceServiceType sourceService;
        private boolean active;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        private Builder() {
        }

        public Builder withId(UUID id) {
            this.id = id;
            return this;
        }

        public Builder withEventType(String eventType) {
            this.eventType = eventType;
            return this;
        }

        public Builder withSourceService(SourceServiceType sourceService) {
            this.sourceService = sourceService;
            return this;
        }

        public Builder withActive(boolean active) {
            this.active = active;
            return this;
        }

        public Builder withCreatedAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Builder withUpdatedAt(LocalDateTime updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        public EventSubscription build() {
            return new EventSubscription(this);
        }
    }
}
