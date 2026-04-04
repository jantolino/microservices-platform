package edu.market.notification.infrastructure.adapter.output.persistence.mapper;

import edu.market.notification.domain.model.TransactionalOutbox;
import edu.market.notification.infrastructure.adapter.output.persistence.entity.TransactionalOutboxEntity;
import org.springframework.stereotype.Component;

/**
 * Mapper para convertir entre el modelo de dominio TransactionalOutbox y la entidad de persistencia TransactionalOutboxEntity.
 */
@Component
public class TransactionalOutboxOutputMapper {

    /**
     * Convierte una entidad de persistencia a un modelo de dominio.
     * 
     * @param entity Entidad de persistencia
     * @return Modelo de dominio
     */
    public TransactionalOutbox toDomain(TransactionalOutboxEntity entity) {
        if (entity == null) {
            return null;
        }
        
        return TransactionalOutbox.builder()
                .withId(entity.getUuid())
                .withStatusType(entity.getStatusType())
                .withAggregateId(entity.getAggregateId())
                .withPayload(entity.getPayload())
                .withCreatedAt(entity.getCreatedAt())
                .withProcessed(entity.isProcessed())
                .withProcessedAt(entity.getProcessedAt())
                .withRetryCount(entity.getRetryCount())
                .withMessage(entity.getMessage())
                .withLastRetryAt(entity.getLastRetryAt())
                .withEventType(entity.getEventType())
                .build();
    }
    
    /**
     * Convierte un modelo de dominio a una entidad de persistencia.
     * 
     * @param domain Modelo de dominio
     * @return Entidad de persistencia
     */
    public TransactionalOutboxEntity toEntity(TransactionalOutbox domain) {
        if (domain == null) {
            return null;
        }
        
        TransactionalOutboxEntity entity = new TransactionalOutboxEntity();
        entity.setUuid(domain.getId());
        entity.setStatusType(domain.getStatusType());
        entity.setAggregateId(domain.getAggregateId());
        entity.setPayload(domain.getPayload());
        entity.setCreatedAt(domain.getCreatedAt());
        entity.setProcessed(domain.isProcessed());
        entity.setProcessedAt(domain.getProcessedAt());
        entity.setRetryCount(domain.getRetryCount());
        entity.setMessage(domain.getMessage());
        entity.setLastRetryAt(domain.getLastRetryAt());
        entity.setEventType(domain.getEventType());
        
        return entity;
    }
}
