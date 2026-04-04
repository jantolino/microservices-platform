package edu.market.notification.infrastructure.adapter.output.persistence.entity;

import edu.market.notification.domain.enums.NotificationChannelType;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/**
 * Entidad de persistencia que representa un canal de notificación asociado a una notificación.
 * Esta clase implementa la relación entre notificaciones y sus canales de envío.
 */
@Entity
@Getter
@Setter
@Table(name = "notification_channels")
public class NotificationChannelEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "notification_uuid", nullable = false)
    private NotificationEntity notification;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "channel", nullable = false)
    private NotificationChannelType channel;
}
