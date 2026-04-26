package edu.market.notification.infrastructure.adapter.output.persistence.mapper;

import edu.market.notification.domain.model.EventSubscription;
import edu.market.notification.infrastructure.adapter.output.persistence.entity.EventSubscriptionEntity;
import org.springframework.stereotype.Component;

/**
 * Mapper para convertir entre entidades de dominio EventSubscription y entidades de persistencia EventSubscriptionEntity.
 */
@Component
public class EventSubscriptionOutputMapper {

    /**
     * Convierte una entidad de persistencia a un objeto de dominio
     * @param entity Entidad de persistencia
     * @return Objeto de dominio
     */
    public EventSubscription toDomain(EventSubscriptionEntity entity) {
        if (entity == null) {
            return null;
        }
        
        return EventSubscription.builder()
                .withId(entity.getUuid())
                .withEventType(entity.getEventType())
                .withSourceService(entity.getSourceService())
                .withActive(entity.isActive())
                .withCreatedAt(entity.getCreatedAt())
                .withUpdatedAt(entity.getUpdatedAt())
                .build();
    }

    /**
     * Convierte un objeto de dominio a una entidad de persistencia
     * @param domain Objeto de dominio
     * @return Entidad de persistencia
     */
    public EventSubscriptionEntity toEntity(EventSubscription domain) {
        if (domain == null) {
            return null;
        }
        
        EventSubscriptionEntity entity = new EventSubscriptionEntity();
        entity.setUuid(domain.getId());
        entity.setEventType(domain.getEventType());
        entity.setSourceService(domain.getSourceService());
        entity.setActive(domain.isActive());
        entity.setCreatedAt(domain.getCreatedAt());
        entity.setUpdatedAt(domain.getUpdatedAt());
        
        return entity;
    }
}
