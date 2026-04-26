package edu.market.notification.infrastructure.adapter.output.persistence.entity;

import edu.market.notification.domain.enums.SourceServiceType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entidad de persistencia que representa una suscripción a eventos de otros microservicios.
 * Esta clase es parte de la capa de infraestructura y se utiliza para mapear
 * la entidad de dominio EventSubscription a una tabla en la base de datos.
 */
@Entity
@Getter
@Setter
@Table(name = "event_subscriptions")
public class EventSubscriptionEntity {
    
    @Id
    @Column(name = "uuid", nullable = false, unique = true)
    private UUID uuid;
    
    @Column(name = "event_type", nullable = false)
    private String eventType;
    
    @Column(name = "source_service", nullable = false)
    private SourceServiceType sourceService;
    
    @Column(name = "active", nullable = false)
    private boolean active;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
