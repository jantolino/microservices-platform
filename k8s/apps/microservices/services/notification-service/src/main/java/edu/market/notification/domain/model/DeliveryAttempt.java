package edu.market.notification.domain.model;

import edu.market.notification.domain.enums.NotificationChannelType;
import edu.market.notification.domain.exception.DeliveryException;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entidad que representa un intento de entrega de una notificación.
 * Parte del patrón de manejo de errores y reintentos.
 */
public class DeliveryAttempt {
    
    private final UUID id;
    private final UUID notificationId;
    private final NotificationChannelType channel;
    private final LocalDateTime attemptedAt;
    private final boolean successful;
    private final String errorMessage;
    private final String providerResponse;
    private final String externalId;
    private final int attemptNumber;

    private DeliveryAttempt(Builder builder) {
        this.id = builder.id != null ? builder.id : UUID.randomUUID();
        this.notificationId = builder.notificationId;
        this.channel = builder.channel;
        this.attemptedAt = builder.attemptedAt != null ? builder.attemptedAt : LocalDateTime.now();
        this.successful = builder.successful;
        this.errorMessage = builder.errorMessage;
        this.providerResponse = builder.providerResponse;
        this.externalId = builder.externalId;
        this.attemptNumber = builder.attemptNumber;
        
        validate();
    }
    
    private void validate() {
        if (notificationId == null) {
            throw new DeliveryException("notificationId required");
        }
        
        if (channel == null) {
            throw new DeliveryException("channel required");
        }

        if (externalId == null) {
            throw new DeliveryException("externalId required");
        }
    }

    public UUID getId() {
        return id;
    }

    public UUID getNotificationId() {
        return notificationId;
    }

    public NotificationChannelType getChannel() {
        return channel;
    }

    public LocalDateTime getAttemptedAt() {
        return attemptedAt;
    }

    public boolean isSuccessful() {
        return successful;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public String getProviderResponse() {
        return providerResponse;
    }

    public String getExternalId() {
        return externalId;
    }

    public int getAttemptNumber() {
        return attemptNumber;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private UUID id;
        private UUID notificationId;
        private NotificationChannelType channel;
        private LocalDateTime attemptedAt;
        private boolean successful;
        private String errorMessage;
        private String providerResponse;
        private String externalId;
        private int attemptNumber;

        public Builder withId(UUID id) {
            this.id = id;
            return this;
        }

        public Builder withNotificationId(UUID notificationId) {
            this.notificationId = notificationId;
            return this;
        }

        public Builder withChannel(NotificationChannelType channel) {
            this.channel = channel;
            return this;
        }

        public Builder withAttemptedAt(LocalDateTime attemptedAt) {
            this.attemptedAt = attemptedAt;
            return this;
        }

        public Builder withSuccessful(boolean successful) {
            this.successful = successful;
            return this;
        }

        public Builder withErrorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
            return this;
        }

        public Builder withProviderResponse(String providerResponse) {
            this.providerResponse = providerResponse;
            return this;
        }

        public Builder withExternalId(String externalId) {
            this.externalId = externalId;
            return this;
        }

        public Builder withAttemptNumber(int attemptNumber) {
            this.attemptNumber = attemptNumber;
            return this;
        }

        public DeliveryAttempt build() {
            return new DeliveryAttempt(this);
        }
    }
}
