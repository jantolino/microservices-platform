package edu.market.notification.infrastructure.adapter.output.persistence.entity;

import edu.market.notification.domain.enums.NotificationChannelType;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;



/**
 * Entidad de persistencia que representa un canal soportado por una plantilla de notificación.
 * Esta clase implementa la relación entre plantillas y sus canales soportados.
 */
@Entity
@Getter
@Setter
@Table(name = "template_supported_channels")
public class TemplateSupportedChannelEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "template_uuid", nullable = false)
    private TemplateEntity template;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "channel", nullable = false)
    private NotificationChannelType channel;
}
