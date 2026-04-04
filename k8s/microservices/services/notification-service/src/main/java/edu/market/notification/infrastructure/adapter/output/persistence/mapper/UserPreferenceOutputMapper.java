package edu.market.notification.infrastructure.adapter.output.persistence.mapper;

import edu.market.notification.domain.enums.NotificationChannelType;
import edu.market.notification.domain.model.UserPreference;
import edu.market.notification.infrastructure.adapter.output.persistence.entity.UserPreferenceChannelEntity;
import edu.market.notification.infrastructure.adapter.output.persistence.entity.UserPreferenceEntity;
import edu.market.notification.infrastructure.adapter.output.persistence.entity.UserPreferenceEventChannelEntity;
import edu.market.notification.infrastructure.adapter.output.persistence.entity.UserPreferenceEventEntity;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Mapper para convertir entre el modelo de dominio UserPreference y la entidad de persistencia UserPreferenceEntity.
 */
@Component
public class UserPreferenceOutputMapper {

    /**
     * Convierte una entidad de persistencia a un modelo de dominio.
     * 
     * @param entity Entidad de persistencia
     * @return Modelo de dominio
     */
    public UserPreference toDomain(UserPreferenceEntity entity) {
        
        if (entity == null) return null;
        
        // Manejo seguro de colecciones nulas con validación adicional
        Set<NotificationChannelType> enabledChannels = Optional.ofNullable(entity.getEnabledChannels())
                .orElse(Collections.emptySet())
                .stream()
                .filter(Objects::nonNull)                
                .map(UserPreferenceChannelEntity::getChannel)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        
        Map<String, Set<NotificationChannelType>> eventPreferences = new LinkedHashMap<>();
        for (UserPreferenceEventEntity eventEntity : Optional.ofNullable(entity.getEventPreferences()).orElse(Collections.emptySet())) {
            if (eventEntity != null && eventEntity.getEventType() != null) {
                Set<NotificationChannelType> channels = Optional.ofNullable(eventEntity.getChannels())
                        .orElse(Collections.emptySet())
                        .stream()
                        .filter(Objects::nonNull)                        
                        .map(UserPreferenceEventChannelEntity::getChannel)
                        .collect(Collectors.toCollection(LinkedHashSet::new));
                eventPreferences.put(eventEntity.getEventType(), channels);
            }
        }        
        UserPreference.Builder builder = UserPreference.builder()
                .withId(entity.getUuid() != null ? entity.getUuid() : UUID.randomUUID())
                .withUserId(entity.getUserId())
                .withGlobalOptOut(entity.isGlobalOptOut())
                .withEnabledChannels(enabledChannels)
                .withCreatedAt(entity.getCreatedAt())
                .withUpdatedAt(entity.getUpdatedAt());
        
        // Asignar cada preferencia de evento usando el builder
        eventPreferences.forEach(builder::withEventPreference);
        
        return builder.build();
    }
    
    /**
     * Convierte un modelo de dominio a una entidad de persistencia.
     * 
     * @param domain Modelo de dominio
     * @return Entidad de persistencia
     */
    public UserPreferenceEntity toEntity(UserPreference domain) {
        
        if (domain == null) return null;
        
        UserPreferenceEntity entity = new UserPreferenceEntity();
        entity.setUuid(domain.getId());
        entity.setUserId(domain.getUserId());
        entity.setGlobalOptOut(domain.isGlobalOptOut());
        
        // Manejo de fechas con valores por defecto
        entity.setCreatedAt(domain.getCreatedAt() != null ? domain.getCreatedAt() : LocalDateTime.now());
        entity.setUpdatedAt(domain.getUpdatedAt() != null ? domain.getUpdatedAt() : entity.getCreatedAt());
        
        // Manejo seguro de colecciones nulas con LinkedHashSet para mantener orden
        Set<UserPreferenceChannelEntity> enabledChannelEntities = Optional.ofNullable(domain.getEnabledChannels())
                .orElse(Collections.emptySet())
                .stream()
                .filter(Objects::nonNull)
                .map(channel -> {
                    UserPreferenceChannelEntity channelEntity = new UserPreferenceChannelEntity();                    
                    channelEntity.setChannel(channel);
                    channelEntity.setUserPreference(entity);
                    return channelEntity;
                })
                .collect(Collectors.toCollection(LinkedHashSet::new));
        entity.setEnabledChannels(enabledChannelEntities);
        
        Set<UserPreferenceEventEntity> eventEntities = new LinkedHashSet<>();
        for (Map.Entry<String, Set<NotificationChannelType>> entry : 
             Optional.ofNullable(domain.getEventPreferences()).orElse(Collections.emptyMap()).entrySet()) {
            
            if (entry.getKey() != null && entry.getValue() != null) {
                UserPreferenceEventEntity eventEntity = new UserPreferenceEventEntity();
                eventEntity.setEventType(entry.getKey());
                eventEntity.setUserPreference(entity);
                
                Set<UserPreferenceEventChannelEntity> channelEntities = entry.getValue().stream()
                        .filter(Objects::nonNull)
                        .map(channel -> {
                            UserPreferenceEventChannelEntity channelEntity = new UserPreferenceEventChannelEntity();                            
                            channelEntity.setChannel(channel);
                            // Esta referencia se establecerá después de guardar el eventEntity
                            return channelEntity;
                        })
                        .collect(Collectors.toCollection(LinkedHashSet::new));
                eventEntity.setChannels(channelEntities);
                eventEntities.add(eventEntity);
            }
        }
        entity.setEventPreferences(eventEntities);
        
        return entity;
    }
}
