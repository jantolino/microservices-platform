package edu.market.notification.infrastructure.adapter.output.persistence.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

/**
 * Entidad de persistencia que representa una preferencia de evento para un usuario.
 * Esta clase implementa la relación entre preferencias de usuario y sus eventos suscritos.
 */
@Entity
@Getter
@Setter
@Table(name = "user_preference_events")
public class UserPreferenceEventEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_preference_uuid", nullable = false)
    private UserPreferenceEntity userPreference;
    
    @Column(name = "event_type", nullable = false)
    private String eventType;
    
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "user_preference_event_id")
    private Set<UserPreferenceEventChannelEntity> channels = new HashSet<>();
}
