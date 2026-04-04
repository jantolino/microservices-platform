package edu.market.notification.infrastructure.adapter.output.persistence.entity;

import edu.market.notification.domain.enums.EventStatusType;
import edu.market.notification.domain.enums.EventType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entidad de persistencia que representa una entrada en el outbox transaccional.
 * Esta clase es parte de la capa de infraestructura y se utiliza para implementar
 * el patrón Outbox para garantizar la consistencia de eventos en transacciones.
 */
@Entity
@Getter
@Setter
@Table(name = "transactional_outbox")
public class TransactionalOutboxEntity {
    
    @Id
    @Column(name = "uuid", nullable = false, unique = true)
    private UUID uuid;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status_type", nullable = false)
    private EventStatusType statusType;
    
    @Column(name = "aggregate_id", nullable = false)
    private String aggregateId;
    
    @Column(name = "payload", nullable = false, columnDefinition = "TEXT")
    private String payload;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "processed", nullable = false)
    private boolean processed;
    
    @Column(name = "processed_at")
    private LocalDateTime processedAt;
    
    @Column(name = "retry_count", nullable = false)
    private int retryCount;
    
    @Column(name = "message")
    private String message;
    
    @Column(name = "last_retry_at")
    private LocalDateTime lastRetryAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false)
    private EventType eventType;
}
