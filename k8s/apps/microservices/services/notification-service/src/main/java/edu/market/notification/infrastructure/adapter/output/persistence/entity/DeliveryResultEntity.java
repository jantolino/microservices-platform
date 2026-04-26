package edu.market.notification.infrastructure.adapter.output.persistence.entity;



import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entidad de persistencia que representa el resultado final de entrega de una notificación.
 * Esta clase es parte de la capa de infraestructura y se utiliza para mapear
 * la entidad de dominio DeliveryResult a una tabla en la base de datos.
 */
@Entity
@Getter
@Setter
@Table(name = "delivery_results")
public class DeliveryResultEntity {
    
    @Id
    @Column(name = "uuid", nullable = false, unique = true)
    private UUID uuid;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "notification_uuid", nullable = false)
    private NotificationEntity notification;
    
    @Column(name = "successful", nullable = false)
    private boolean successful;
    
    @Column(name = "delivery_time_ms")
    private Long deliveryTimeMs;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "audit_id")
    private NotificationAuditEntity audit;
    
    @Column(name = "retry_count")
    private Integer retryCount;
}
