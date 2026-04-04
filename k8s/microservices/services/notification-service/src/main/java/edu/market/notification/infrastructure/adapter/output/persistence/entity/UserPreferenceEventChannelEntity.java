package edu.market.notification.infrastructure.adapter.output.persistence.entity;

import edu.market.notification.domain.enums.NotificationChannelType;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/**
 * Entidad de persistencia que representa un canal habilitado para un evento específico en las preferencias de usuario.
 * Esta clase implementa la relación entre eventos de preferencia y sus canales habilitados.
 */
@Entity
@Getter
@Setter
@Table(name = "user_preference_event_channels")
public class UserPreferenceEventChannelEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_preference_event_id", nullable = false)
    private UserPreferenceEventEntity userPreferenceEvent;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "channel", nullable = false)
    private NotificationChannelType channel;
}
