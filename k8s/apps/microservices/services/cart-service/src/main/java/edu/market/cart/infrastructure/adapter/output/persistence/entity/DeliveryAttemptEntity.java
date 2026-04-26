package edu.market.notification.infrastructure.adapter.output.persistence.entity;

import edu.market.notification.domain.enums.NotificationChannelType;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entidad de persistencia que representa un intento de entrega de notificación.
 * Esta clase es parte de la capa de infraestructura y se utiliza para mapear
 * la entidad de dominio DeliveryAttempt a una tabla en la base de datos.
 */
@Entity
@Getter
@Setter
@Table(name = "delivery_attempts")
public class DeliveryAttemptEntity {
    
    @Id
    @Column(name = "uuid", nullable = false, unique = true)
    private UUID uuid;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "notification_uuid", nullable = false)
    private NotificationEntity notification;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "channel", nullable = false)
    private NotificationChannelType channel;
    
    @Column(name = "attempted_at", nullable = false)
    private LocalDateTime attemptedAt;
    
    @Column(name = "successful", nullable = false)
    private boolean successful;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;
    
    @Column(name = "provider_response", columnDefinition = "TEXT")
    private String providerResponse;
    
    @Column(name = "external_id")
    private String externalId;
    
    @Column(name = "attempt_number", nullable = false)
    private int attemptNumber;
}
