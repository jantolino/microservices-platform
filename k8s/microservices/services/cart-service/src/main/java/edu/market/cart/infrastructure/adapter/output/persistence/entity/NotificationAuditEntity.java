package edu.market.notification.infrastructure.adapter.output.persistence.entity;

import edu.market.notification.domain.enums.NotificationChannelType;
import edu.market.notification.domain.enums.EventStatusType;
import edu.market.notification.domain.enums.SourceServiceType;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entidad de persistencia que representa un registro de auditoría de una notificación.
 * Esta clase es parte de la capa de infraestructura y se utiliza para mapear
 * la entidad de dominio NotificationAudit a una tabla en la base de datos.
 */
@Entity
@Getter
@Setter
@Table(name = "notification_audits")
public class NotificationAuditEntity {
    
    @Id
    @Column(name = "uuid", nullable = false, unique = true)
    private UUID uuid;
    
    @Column(name = "notification_uuid", nullable = false)
    private UUID notificationId;
    
    @Column(name = "action", nullable = false)
    private String action;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private EventStatusType status;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "channel", nullable = false)
    private NotificationChannelType channel;
    
    @Column(name = "details", columnDefinition = "TEXT")
    private String details;
    
    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;
    
    @Column(name = "user_id", nullable = false)
    private UUID userId;
    
    @Column(name = "requester_id", nullable = false)
    private UUID requesterId;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "source_service")
    private SourceServiceType sourceService;
    
    @Column(name = "ip_address")
    private String ipAddress;
    
    @Column(name = "user_agent")
    private String userAgent;
    
    @Column(name = "device_info")
    private String deviceInfo;
}
