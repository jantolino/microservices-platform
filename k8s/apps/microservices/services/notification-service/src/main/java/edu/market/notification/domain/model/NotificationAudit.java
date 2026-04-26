package edu.market.notification.domain.model;

import edu.market.notification.domain.enums.NotificationChannelType;
import edu.market.notification.domain.enums.EventStatusType;
import edu.market.notification.domain.enums.SourceServiceType;
import edu.market.notification.domain.exception.InvalidEntityException;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entidad que registra la auditoría de las notificaciones.
 * Implementa el patrón de Audit Log para mantener un registro histórico
 * de las operaciones realizadas sobre las notificaciones.
 */
public class NotificationAudit {
    
    private final UUID id;
    private final UUID notificationId;
    private final String action;
    private final EventStatusType status;
    private final NotificationChannelType channel;
    private final String details;
    private final LocalDateTime timestamp;
    private final UUID userId;              // Usuario que recibe la notificación
    private final UUID requesterId;         // Usuario que solicitó la acción
    private final SourceServiceType sourceService; // Servicio que originó la solicitud
    private final String ipAddress;
    private final String userAgent;
    private final String deviceInfo;

    private NotificationAudit(Builder builder) {
        this.id = builder.id != null ? builder.id : UUID.randomUUID();
        this.notificationId = builder.notificationId;
        this.action = builder.action;
        this.status = builder.status;
        this.channel = builder.channel;
        this.details = builder.details;
        this.timestamp = builder.timestamp != null ? builder.timestamp : LocalDateTime.now();
        this.userId = builder.userId;
        this.requesterId = builder.requesterId;
        this.sourceService = builder.sourceService != null ? builder.sourceService : SourceServiceType.UNKNOWN;
        this.ipAddress = builder.ipAddress;
        this.userAgent = builder.userAgent;
        this.deviceInfo = builder.deviceInfo;
        
        validate();
    }
    
    private void validate() {
        if (notificationId == null) {
            throw new InvalidEntityException("notificationId required");
        }
        
        if (action == null || action.trim().isEmpty()) {
            throw new InvalidEntityException("action required");
        }
        
        if (status == null) {
            throw new InvalidEntityException("status required");
        }
        
        if (channel == null) {
            throw new InvalidEntityException("channel required");
        }
        
        if (userId == null) {
            throw new InvalidEntityException("userId required");
        }
        
        if (requesterId == null) {
            throw new InvalidEntityException("requesterId required");
       }
    }

    public UUID getId() {
        return id;
    }

    public UUID getNotificationId() {
        return notificationId;
    }

    public String getAction() {
        return action;
    }

    public EventStatusType getStatus() {
        return status;
    }

    public NotificationChannelType getChannel() {
        return channel;
    }

    public String getDetails() {
        return details;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public UUID getUserId() {
        return userId;
    }
    
    public UUID getRequesterId() {
        return requesterId;
    }
    
    public SourceServiceType getSourceService() {
        return sourceService;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public String getDeviceInfo() {
        return deviceInfo;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private UUID id;
        private UUID notificationId;
        private String action;
        private EventStatusType status;
        private NotificationChannelType channel;
        private String details;
        private LocalDateTime timestamp;
        private UUID userId;
        private UUID requesterId;
        private SourceServiceType sourceService;
        private String ipAddress;
        private String userAgent;
        private String deviceInfo;

        public Builder withId(UUID id) {
            this.id = id;
            return this;
        }

        public Builder withNotificationId(UUID notificationId) {
            this.notificationId = notificationId;
            return this;
        }

        public Builder withAction(String action) {
            this.action = action;
            return this;
        }

        public Builder withStatus(EventStatusType status) {
            this.status = status;
            return this;
        }

        public Builder withChannel(NotificationChannelType channel) {
            this.channel = channel;
            return this;
        }

        public Builder withDetails(String details) {
            this.details = details;
            return this;
        }

        public Builder withTimestamp(LocalDateTime timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public Builder withUserId(UUID userId) {
            this.userId = userId;
            return this;
        }
        
        public Builder withRequesterId(UUID requesterId) {
            this.requesterId = requesterId;
            return this;
        }
        
        public Builder withSourceService(SourceServiceType sourceService) {
            this.sourceService = sourceService;
            return this;
        }

        public Builder withIpAddress(String ipAddress) {
            this.ipAddress = ipAddress;
            return this;
        }

        public Builder withUserAgent(String userAgent) {
            this.userAgent = userAgent;
            return this;
        }

        public Builder withDeviceInfo(String deviceInfo) {
            this.deviceInfo = deviceInfo;
            return this;
        }

        public NotificationAudit build() {
            return new NotificationAudit(this);
        }
    }
}
