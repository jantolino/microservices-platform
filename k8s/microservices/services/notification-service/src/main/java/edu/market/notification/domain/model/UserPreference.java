package edu.market.notification.domain.model;

import edu.market.notification.domain.enums.NotificationChannelType;
import edu.market.notification.domain.exception.UserPreferenceException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Entidad que representa las preferencias de notificación de un usuario.
 * Permite configurar qué tipos de eventos desea recibir y por qué canales.
 * 
 * Patrones de diseño implementados:
 * - Entity: Implementa el concepto de Entidad de Dominio con identificador único (UUID)
 * - Builder: Utiliza el patrón Builder para la construcción flexible de preferencias
 * - Defensive Copy: Implementa copias defensivas en los getters de colecciones para prevenir modificaciones externas
 * - Validator: Incluye validación interna de reglas de negocio en el método validate()
 * - Rich Domain Model: Encapsula comportamiento y reglas de negocio dentro de la entidad
 * - Immutable Object: Los atributos principales son finales para garantizar la integridad
 */
public class UserPreference {
    
    private final UUID id;
    private final UUID userId;
    private final Map<String, Set<NotificationChannelType>> eventPreferences;
    private final Set<NotificationChannelType> enabledChannels;
    private boolean globalOptOut;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private UserPreference(Builder builder) {
        this.id = builder.id != null ? builder.id : UUID.randomUUID();
        this.userId = builder.userId;
        this.eventPreferences = new HashMap<>(builder.eventPreferences);
        this.enabledChannels = new HashSet<>(builder.enabledChannels);
        this.globalOptOut = builder.globalOptOut;
        this.createdAt = builder.createdAt != null ? builder.createdAt : LocalDateTime.now();
        this.updatedAt = builder.updatedAt != null ? builder.updatedAt : this.createdAt;
        
        validate();
    }
    
    private void validate() {
        if (userId == null) {
            throw new UserPreferenceException("userId required");
        }
    }

    public UUID getId() {
        return id;
    }

    public UUID getUserId() {
        return userId;
    }

    public Map<String, Set<NotificationChannelType>> getEventPreferences() {
        // Devuelve una copia defensiva para evitar modificaciones externas
        Map<String, Set<NotificationChannelType>> copy = new HashMap<>();
        for (Map.Entry<String, Set<NotificationChannelType>> entry : eventPreferences.entrySet()) {
            copy.put(entry.getKey(), new HashSet<>(entry.getValue()));
        }
        return copy;
    }

    /**
     * Obtiene los canales habilitados globalmente para el usuario
     * @return Conjunto de canales habilitados
     */
    public Set<NotificationChannelType> getEnabledChannels() {
        // Devuelve una copia defensiva para evitar modificaciones externas
        return new HashSet<>(enabledChannels);
    }

    public boolean isGlobalOptOut() {
        return globalOptOut;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    /**
     * Verifica si el usuario está suscrito a un tipo de evento específico
     * @param eventType Tipo de evento
     * @return true si está suscrito
     */
    public boolean isSubscribedToEvent(String eventType) {
        if (globalOptOut) {
            return false;
        }
        return eventPreferences.containsKey(eventType) && !eventPreferences.get(eventType).isEmpty();
    }

    /**
     * Verifica si el usuario está suscrito a un tipo de evento por un canal específico
     * @param eventType Tipo de evento
     * @param channel Canal de notificación
     * @return true si está suscrito por ese canal
     */
    public boolean isSubscribedToEventViaChannel(String eventType, NotificationChannelType channel) {
        if (globalOptOut) {
            return false;
        }
        return eventPreferences.containsKey(eventType) && 
               eventPreferences.get(eventType).contains(channel);
    }
    
    /**
     * Verifica si el usuario está suscrito a un tipo de evento por al menos uno de los canales especificados
     * @param eventType Tipo de evento
     * @param channels Conjunto de canales de notificación
     * @return true si está suscrito por al menos uno de los canales
     */
    public boolean isSubscribedToEventViaChannel(String eventType, Set<NotificationChannelType> channels) {
        if (globalOptOut || channels == null || channels.isEmpty()) {
            return false;
        }
        
        if (!eventPreferences.containsKey(eventType)) {
            return false;
        }
        
        // Verificar si hay al menos un canal en común entre los canales preferidos del usuario
        // y los canales de la notificación
        Set<NotificationChannelType> preferredChannels = eventPreferences.get(eventType);
        for (NotificationChannelType channel : channels) {
            if (preferredChannels.contains(channel)) {
                return true;
            }
        }
        
        return false;
    }

    /**
     * Suscribe al usuario a un tipo de evento por un canal específico
     * @param eventType Tipo de evento
     * @param channel Canal de notificación
     */
    public void subscribeToEventViaChannel(String eventType, NotificationChannelType channel) {
        if (!eventPreferences.containsKey(eventType)) {
            eventPreferences.put(eventType, new HashSet<>());
        }
        eventPreferences.get(eventType).add(channel);
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Desuscribe al usuario de un tipo de evento por un canal específico
     * @param eventType Tipo de evento
     * @param channel Canal de notificación
     */
    public void unsubscribeFromEventViaChannel(String eventType, NotificationChannelType channel) {
        if (eventPreferences.containsKey(eventType)) {
            eventPreferences.get(eventType).remove(channel);
            if (eventPreferences.get(eventType).isEmpty()) {
                eventPreferences.remove(eventType);
            }
            this.updatedAt = LocalDateTime.now();
        }
    }

    /**
     * Desuscribe al usuario de un tipo de evento por todos los canales
     * @param eventType Tipo de evento
     */
    public void unsubscribeFromEvent(String eventType) {
        eventPreferences.remove(eventType);
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Establece la preferencia global de opt-out
     * @param optOut true para desuscribirse de todas las notificaciones
     */
    public void setGlobalOptOut(boolean optOut) {
        this.globalOptOut = optOut;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Obtiene los canales preferidos para un tipo de evento
     * @param eventType Tipo de evento
     * @return Conjunto de canales o conjunto vacío si no está suscrito
     */
    public Set<NotificationChannelType> getPreferredChannelsForEvent(String eventType) {
        if (globalOptOut || !eventPreferences.containsKey(eventType)) {
            return new HashSet<>();
        }
        return new HashSet<>(eventPreferences.get(eventType));
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private UUID id;
        private UUID userId;
        private Map<String, Set<NotificationChannelType>> eventPreferences = new HashMap<>();
        private Set<NotificationChannelType> enabledChannels = new HashSet<>();
        private boolean globalOptOut = false;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public Builder withId(UUID id) {
            this.id = id;
            return this;
        }

        public Builder withUserId(UUID userId) {
            this.userId = userId;
            return this;
        }

        public Builder withEventPreference(String eventType, Set<NotificationChannelType> channels) {
            if (channels != null && !channels.isEmpty()) {
                this.eventPreferences.put(eventType, new HashSet<>(channels));
            }
            return this;
        }

        public Builder withEnabledChannels(Set<NotificationChannelType> channels) {
            if (channels != null) {
                this.enabledChannels = new HashSet<>(channels);
            }
            return this;
        }

        public Builder withEnabledChannel(NotificationChannelType channel) {
            if (channel != null) {
                this.enabledChannels.add(channel);
            }
            return this;
        }

        public Builder withGlobalOptOut(boolean globalOptOut) {
            this.globalOptOut = globalOptOut;
            return this;
        }

        public Builder withCreatedAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Builder withUpdatedAt(LocalDateTime updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        public UserPreference build() {
            return new UserPreference(this);
        }
    }
}
