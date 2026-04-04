package edu.market.notification.infrastructure.adapter.output.persistence.entity;

import edu.market.notification.domain.enums.SourceServiceType;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Entidad de persistencia que representa una plantilla de notificación en la base de datos.
 * Esta clase es parte de la capa de infraestructura y se utiliza para mapear
 * la entidad de dominio Template a una tabla en la base de datos.
 */
@Entity
@Getter
@Setter
@Table(name = "templates")
public class TemplateEntity {
    
    @Id
    @Column(name = "uuid", nullable = false, unique = true)
    private UUID uuid;
    
    @Column(name = "code", nullable = false, unique = true)
    private String code;
    
    @Column(name = "name", nullable = false)
    private String name;
    
    @Column(name = "content", nullable = false, length = 4000)
    private String content;
    
    @Column(name = "subject")
    private String subject;
    
    @Column(name = "language", length = 10)
    private String language;
    
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "template_uuid")
    private Set<TemplateSupportedChannelEntity> supportedChannelEntities = new HashSet<>();
    
    @Column(name = "version", nullable = false)
    private Integer version;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Column(name = "active", nullable = false)
    private Boolean active;
    
    @Column(name = "created_by")
    private UUID createdBy;
    
    @Column(name = "updated_by")
    private UUID updatedBy;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "source_service", nullable = false)
    private SourceServiceType sourceService;
}
    
