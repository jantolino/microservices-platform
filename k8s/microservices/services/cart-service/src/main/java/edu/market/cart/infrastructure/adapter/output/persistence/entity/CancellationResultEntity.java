package edu.market.notification.infrastructure.adapter.output.persistence.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Entidad de persistencia que representa el resultado de la cancelación de una notificación.
 * Esta clase es parte de la capa de infraestructura y se utiliza para mapear
 * la entidad de dominio CancellationResult a una tabla en la base de datos.
 */
@Entity
@Getter
@Setter
@Table(name = "cancellation_results")
public class CancellationResultEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;
    
    @Column(name = "successful", nullable = false)
    private boolean successful;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "notification_uuid", nullable = false)
    private NotificationEntity notification;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "audit_uuid")
    private NotificationAuditEntity audit;
    
    @Column(name = "cancelled_at", nullable = false)
    private LocalDateTime cancelledAt;
}
