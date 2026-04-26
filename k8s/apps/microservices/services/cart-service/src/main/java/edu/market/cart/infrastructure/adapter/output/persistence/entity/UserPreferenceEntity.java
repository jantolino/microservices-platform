package edu.market.notification.infrastructure.adapter.output.persistence.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Entidad de persistencia que representa las preferencias de notificación de un usuario.
 * Esta clase es parte de la capa de infraestructura y se utiliza para mapear
 * la entidad de dominio UserPreference a una tabla en la base de datos.
 */
@Entity
@Getter
@Setter
@Table(name = "user_preferences")
public class UserPreferenceEntity {
    
    @Id
    @Column(name = "uuid", nullable = false, unique = true)
    private UUID uuid;
    
    @Column(name = "user_id", nullable = false)
    private UUID userId;
    
    @Column(name = "global_opt_out", nullable = false)
    private boolean globalOptOut = false;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @OneToMany(mappedBy = "userPreference", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<UserPreferenceChannelEntity> enabledChannels = new HashSet<>();
    
    @OneToMany(mappedBy = "userPreference", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<UserPreferenceEventEntity> eventPreferences = new HashSet<>();
}
