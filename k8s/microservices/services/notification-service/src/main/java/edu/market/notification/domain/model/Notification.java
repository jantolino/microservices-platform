package edu.market.notification.domain.model;

import edu.market.notification.domain.enums.NotificationChannelType;
import edu.market.notification.domain.enums.NotificationPriorityType;
import edu.market.notification.domain.enums.EventStatusType;
import edu.market.notification.domain.enums.SourceServiceType;
import edu.market.notification.domain.exception.NotificationException;
import edu.market.notification.domain.vo.NotificationContentVO;
import edu.market.notification.domain.vo.RecipientVO;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

/**
 * Entidad principal que representa una notificación en el sistema.
 * 
 * Patrones de diseño implementados:
 * - Entity: Implementa el concepto de Entidad de Dominio con identificador único (UUID)
 * - Builder: Utiliza el patrón Builder para la construcción flexible de notificaciones
 * - State: Maneja transiciones de estado controladas a través de métodos como markAsSent(),
 *   markAsFailed(), schedule() y cancel()
 * - Immutable Object: Los atributos principales son finales para garantizar la integridad
 * - Rich Domain Model: Encapsula comportamiento y reglas de negocio dentro de la entidad
 */
public class Notification {
    
    private final UUID id;
    private final String subject;
    private final NotificationContentVO content;
    private final RecipientVO recipient;
    private final Set<NotificationChannelType> channels;
    private final NotificationPriorityType priority;
    private EventStatusType status;
    private final LocalDateTime createdAt;
    private LocalDateTime scheduledFor;
    private LocalDateTime sentAt;
    private Integer retryCount;
    private String errorMessage;
    private UUID templateId;
    private final UUID requesterId;           // ID del usuario que solicitó la notificación
    private final SourceServiceType sourceService; // Servicio que originó la solicitud

    private Notification(Builder builder) {
        this.id = builder.id != null ? builder.id : UUID.randomUUID();
        this.subject = builder.subject;
        this.content = builder.content;
        this.recipient = builder.recipient;
        this.channels = builder.channels;
        this.priority = builder.priority;
        this.status = builder.status;
        this.createdAt = builder.createdAt != null ? builder.createdAt : LocalDateTime.now();
        this.scheduledFor = builder.scheduledFor;
        this.sentAt = builder.sentAt;
        this.retryCount = builder.retryCount != null ? builder.retryCount : 0;
        this.errorMessage = builder.errorMessage;
        this.templateId = builder.templateId;
        this.requesterId = builder.requesterId;
        this.sourceService = builder.sourceService != null ? builder.sourceService : SourceServiceType.UNKNOWN;
        
        validate();
    }
    
    private void validate() {
        if (subject == null || subject.isBlank()) {
            throw new NotificationException("subject required");
        }
        
        if (content == null) {
            throw new NotificationException("content required");
        }
        
        if (recipient == null) {
            throw new NotificationException("recipient required");
        }
        
        if (channels == null || channels.isEmpty()) {
            throw new NotificationException("channels required");
        }
        
        if (priority == null) {
            throw new NotificationException("priority required");
        }
        
        if (status == null) {
            throw new NotificationException("status required");
        }
        
        if (requesterId == null) {
            throw new NotificationException("requesterId required");
        }
    }

    public UUID getId() {
        return id;
    }

    public String getSubject() {
        return subject;
    }

    public NotificationContentVO getContent() {
        return content;
    }

    public RecipientVO getRecipient() {
        return recipient;
    }

    public Set<NotificationChannelType> getChannels() {
        return channels;
    }

    public NotificationPriorityType getPriority() {
        return priority;
    }

    public EventStatusType getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getScheduledFor() {
        return scheduledFor;
    }

    public LocalDateTime getSentAt() {
        return sentAt;
    }

    public Integer getRetryCount() {
        return retryCount;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public UUID getTemplateId() {
        return templateId;
    }
    
    public UUID getRequesterId() {
        return requesterId;
    }
    
    public SourceServiceType getSourceService() {
        return sourceService;
    }

    /**
     * Marca la notificación como enviada
     */
    public void markAsSent() {
        this.status = EventStatusType.SENT;
        this.sentAt = LocalDateTime.now();
    }

    /**
     * Marca la notificación como fallida
     * @param errorMessage Mensaje de error
     */
    public void markAsFailed(String errorMessage) {
        this.status = EventStatusType.FAILED;
        this.errorMessage = errorMessage;
        this.retryCount++;
    }

    /**
     * Programa la notificación para una fecha futura
     * @param scheduledFor Fecha programada
     */
    public void schedule(LocalDateTime scheduledFor) {
        if (scheduledFor.isBefore(LocalDateTime.now())) {
            throw new NotificationException("Scheduled date must be greater than current date");
        }
        this.scheduledFor = scheduledFor;
        this.status = EventStatusType.SCHEDULED;
    }

    /**
     * Cancela una notificación programada
     */
    public void cancel() {
        if (this.status != EventStatusType.SCHEDULED) {
            throw new NotificationException("Only scheduled notifications can be cancelled");
        }
        this.status = EventStatusType.CANCELLED;
    }

    /**
     * Verifica si la notificación está lista para ser enviada
     */
    public boolean isReadyToSend() {
        if (status == EventStatusType.SCHEDULED) {
            return scheduledFor != null && scheduledFor.isBefore(LocalDateTime.now());
        }
        return status == EventStatusType.PENDING;
    }

    /**
     * Verifica si la notificación puede ser reintentada
     * @param maxRetries Número máximo de reintentos
     */
    public boolean canRetry(int maxRetries) {
        return status == EventStatusType.FAILED && retryCount < maxRetries;
    }
    
    /**
     * Incrementa el contador de reintentos de la notificación
     */
    public void incrementRetryCount() {
        this.retryCount = this.retryCount ++;
    }
    
    /**
     * Marca la notificación como pendiente para ser enviada
     */
    public void markAsPending() {
        if (this.status != EventStatusType.FAILED) {
            throw new NotificationException("Only failed notifications can be marked as pending");
        }
        this.status = EventStatusType.PENDING;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private UUID id;
        private String subject;
        private NotificationContentVO content;
        private RecipientVO recipient;
        private Set<NotificationChannelType> channels;
        private NotificationPriorityType priority = NotificationPriorityType.NORMAL;
        private EventStatusType status = EventStatusType.PENDING;
        private LocalDateTime createdAt;
        private LocalDateTime scheduledFor;
        private LocalDateTime sentAt;
        private Integer retryCount;
        private String errorMessage;
        private UUID templateId;
        private UUID requesterId;
        private SourceServiceType sourceService;

        public Builder withId(UUID id) {
            this.id = id;
            return this;
        }

        public Builder withSubject(String subject) {
            this.subject = subject;
            return this;
        }

        public Builder withContent(NotificationContentVO content) {
            this.content = content;
            return this;
        }

        public Builder withRecipient(RecipientVO recipient) {
            this.recipient = recipient;
            return this;
        }

        public Builder withChannels(Set<NotificationChannelType> channels) {
            this.channels = channels;
            return this;
        }        

        public Builder withPriority(NotificationPriorityType priority) {
            this.priority = priority;
            return this;
        }

        public Builder withStatus(EventStatusType status) {
            this.status = status;
            return this;
        }

        public Builder withCreatedAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Builder withScheduledFor(LocalDateTime scheduledFor) {
            this.scheduledFor = scheduledFor;
            return this;
        }

        public Builder withSentAt(LocalDateTime sentAt) {
            this.sentAt = sentAt;
            return this;
        }

        public Builder withRetryCount(Integer retryCount) {
            this.retryCount = retryCount;
            return this;
        }

        public Builder withErrorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
            return this;
        }

        public Builder withTemplateId(UUID templateId) {
            this.templateId = templateId;
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

        public Notification build() {
            return new Notification(this);
        }
    }
}
