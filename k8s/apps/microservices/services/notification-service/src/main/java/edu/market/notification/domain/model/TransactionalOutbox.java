package edu.market.notification.domain.model;

import edu.market.notification.domain.enums.EventStatusType;
import edu.market.notification.domain.enums.EventType;
import edu.market.notification.domain.exception.OutboxException;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Modelo de dominio para eventos en la tabla outbox.
 * 
 * Este modelo representa un evento que debe ser procesado de forma asíncrona
 * siguiendo el patrón Transactional Outbox. Los eventos se guardan en una tabla
 * de outbox como parte de la transacción principal y luego son procesados
 * por un proceso separado.
 * 
 * Esta clase es inmutable, todos sus atributos son finales y no se pueden modificar
 * después de la creación.
 */
public class TransactionalOutbox {
    
    private final UUID id;
    private EventStatusType statusType;
    private final String aggregateId;
    private final String payload;
    private final LocalDateTime createdAt;
    private boolean processed;
    private LocalDateTime processedAt;
    private int retryCount;    
    private String message;
    private LocalDateTime lastRetryAt;
    private final EventType eventType;
    
    /**
     * Constructor con todos los campos.
     * 
     * @param id Identificador único del evento
     * @param eventType Tipo de evento (ej: SENT, FAILED)
     * @param aggregateId Identificador del agregado relacionado (ej: ID de notificación)
     * @param payload Contenido serializado del evento
     * @param createdAt Fecha y hora de creación
     * @param processed Indica si el evento ha sido procesado
     * @param processedAt Fecha y hora de procesamiento, puede ser null
     * @param retryCount Número de reintentos
     * @param message Mensaje del evento
     * @param lastRetryAt Fecha y hora del último reintento
     * @param eventType Tipo de evento
     */
    private TransactionalOutbox(UUID id, EventStatusType statusType, String aggregateId, String payload, 
                       LocalDateTime createdAt, boolean processed, LocalDateTime processedAt,
                        int retryCount, String message, LocalDateTime lastRetryAt, EventType eventType) {
        this.id = id;
        this.statusType = statusType;
        this.aggregateId = aggregateId;
        this.payload = payload;
        this.createdAt = createdAt;
        this.processed = processed;
        this.processedAt = processedAt;
        this.retryCount = retryCount;
        this.message = message;
        this.lastRetryAt = lastRetryAt;
        this.eventType = eventType;
        
        validate();
    }
    
    private void validate() {
        if (statusType == null) {
            throw new OutboxException("statusType required");
        }
        
        if (aggregateId == null || aggregateId.isBlank()) {
            throw new OutboxException("aggregateId required");
        }
        
        if (payload == null || payload.isBlank()) {
            throw new OutboxException("payload required");
        }
        
        if (processed && processedAt == null) {
            throw new OutboxException("processedAt required when processed is true");
        }
    }
    
    /**
     * Crea una nueva instancia del evento marcada como procesada.
     * 
     * @param processedAt Fecha y hora de procesamiento
     * @return Una nueva instancia de OutboxEvent con el estado actualizado
     */
    public void markAsProcessed() {
        this.processed = true;
        this.processedAt = LocalDateTime.now();
    }    
    
    /**
     * Crea una nueva instancia del evento marcada como cancelada.
     * 
     * @param errorMessage Mensaje de error que explica la razón de la cancelación
     * @return Una nueva instancia de OutboxEvent con el estado actualizado a CANCELLED
     */
    public void markAsCancelled(String errorMessage) {
        this.statusType = EventStatusType.CANCELLED;
        this.message = errorMessage;
        this.processed = true;
        this.processedAt = LocalDateTime.now();
    }
    
    public void markAsFailed(String errorMessage) {
        this.statusType = EventStatusType.FAILED;
        this.message = errorMessage;
        this.processed = false;
        this.processedAt = LocalDateTime.now();
    }

    /**
     * Incrementa el contador de reintentos y actualiza la fecha del último reintento.
     * 
     * @param lastRetryAt Fecha y hora del último reintento
     * @return Una nueva instancia con el contador incrementado
     */
    public void incrementRetryCount() {
        this.retryCount++;
        this.lastRetryAt = LocalDateTime.now();
    }

    // Getters
    
    public UUID getId() {
        return id;
    }
    
    public EventStatusType getStatusType() {
        return statusType;
    }
    
    public String getAggregateId() {
        return aggregateId;
    }
    
    public String getPayload() {
        return payload;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public boolean isProcessed() {
        return processed;
    }
    
    public LocalDateTime getProcessedAt() {
        return processedAt;
    }
    
    public int getRetryCount() {
        return retryCount;
    }
    
    public String getMessage() {
        return message;
    }
    
    public LocalDateTime getLastRetryAt() {
        return lastRetryAt;
    }
    
    public EventType getEventType() {
        return eventType;
    }
    
    /**
     * Builder para crear instancias de OutboxEvent de forma más fluida.
     */
    public static Builder builder() {
        return new Builder();
    }
    
    /**
     * Clase Builder para OutboxEvent.
     */
    public static class Builder {
        private UUID id;
        private EventStatusType statusType;
        private String aggregateId;
        private String payload;
        private LocalDateTime createdAt;
        private boolean processed = false;
        private LocalDateTime processedAt = null;
        private int retryCount = 0;
        private String message;
        private LocalDateTime lastRetryAt = null;
        private EventType eventType;
        
        public Builder withId(UUID id) {
            this.id = id;
            return this;
        }
        
        public Builder withStatusType(EventStatusType statusType) {
            this.statusType = statusType;
            return this;
        }
        
        public Builder withAggregateId(String aggregateId) {
            this.aggregateId = aggregateId;
            return this;
        }
        
        public Builder withPayload(String payload) {
            this.payload = payload;
            return this;
        }
        
        public Builder withCreatedAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }
        
        public Builder withProcessed(boolean processed) {
            this.processed = processed;
            return this;
        }
        
        public Builder withProcessedAt(LocalDateTime processedAt) {
            this.processedAt = processedAt;
            return this;
        }
        
        public Builder withRetryCount(int retryCount) {
            this.retryCount = retryCount;
            return this;
        }
        
        public Builder withMessage(String message) {
            this.message = message;
            return this;
        }
        
        public Builder withLastRetryAt(LocalDateTime lastRetryAt) {
            this.lastRetryAt = lastRetryAt;
            return this;
        }
        
        public Builder withEventType(EventType eventType) {
            this.eventType = eventType;
            return this;
        }
        
        public TransactionalOutbox build() {
            return new TransactionalOutbox(
                this.id != null ? this.id : UUID.randomUUID(),
                this.statusType,
                this.aggregateId,
                this.payload,
                this.createdAt != null ? this.createdAt : LocalDateTime.now(),
                this.processed,
                this.processedAt,
                this.retryCount,
                this.message,
                this.lastRetryAt,
                this.eventType
            );
        }
    }
}
