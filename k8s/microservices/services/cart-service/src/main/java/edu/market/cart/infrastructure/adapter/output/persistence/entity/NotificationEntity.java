package edu.market.notification.infrastructure.adapter.output.persistence.entity;

import edu.market.notification.domain.enums.NotificationPriorityType;
import edu.market.notification.domain.enums.EventStatusType;
import edu.market.notification.domain.enums.SourceServiceType;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Entidad de persistencia que representa una notificación en la base de datos.
 * Esta clase es parte de la capa de infraestructura y se utiliza para mapear
 * la entidad de dominio Notification a una tabla en la base de datos.
 */
@Entity
@Getter
@Setter
@Table(name = "notifications")
public class NotificationEntity {
    
    @Id
    @Column(name = "uuid", nullable = false, unique = true)
    private UUID uuid;
    
    @Column(name = "subject", nullable = false)
    private String subject;
    
    @Column(name = "content_body", nullable = false, length = 4000)
    private String contentBody;
    
    @Column(name = "content_attributes", columnDefinition = "jsonb")
    private String contentAttributes;
    
    @Column(name = "recipient_user_id")
    private UUID recipientUserId;
    
    @Column(name = "recipient_email")
    private String recipientEmail;
    
    @Column(name = "recipient_phone")
    private String recipientPhone;
    
    @Column(name = "recipient_device_token")
    private String recipientDeviceToken;
    
    @Column(name = "recipient_attributes", columnDefinition = "jsonb")
    private String recipientAttributes;
    
    @OneToMany(mappedBy = "notification", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<NotificationChannelEntity> channelEntities = new HashSet<>();
    
    @Enumerated(EnumType.STRING)
    @Column(name = "priority", nullable = false)
    private NotificationPriorityType priority;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private EventStatusType status;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "scheduled_for")
    private LocalDateTime scheduledFor;
    
    @Column(name = "sent_at")
    private LocalDateTime sentAt;
    
    @Column(name = "retry_count")
    private Integer retryCount;
    
    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;
    
    @Column(name = "template_id")
    private UUID templateId;
    
    @Column(name = "requester_id", nullable = false)
    private UUID requesterId;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "source_service", nullable = false)
    private SourceServiceType sourceService;    
   
}
