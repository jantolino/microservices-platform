package edu.market.notification.infrastructure.adapter.output.persistence.entity;

import edu.market.notification.domain.enums.NotificationChannelType;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/**
 * Entidad de persistencia que representa un canal habilitado para las preferencias de un usuario.
 * Esta clase implementa la relación entre preferencias de usuario y sus canales habilitados.
 */
@Entity
@Getter
@Setter
@Table(name = "user_preference_channels")
public class UserPreferenceChannelEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_preference_uuid", nullable = false)
    private UserPreferenceEntity userPreference;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "channel", nullable = false)
    private NotificationChannelType channel;
}
