package edu.market.notification.infrastructure.adapter.output.persistence.mapper;

import edu.market.notification.domain.model.NotificationAudit;
import edu.market.notification.infrastructure.adapter.output.persistence.entity.NotificationAuditEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Mapper para convertir entre NotificationAudit del dominio y NotificationAuditEntity de persistencia.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationAuditOutputMapper {

    public NotificationAudit toDomain(NotificationAuditEntity entity) {
        if (entity == null) {
            log.warn("toDomain - Received null entity");
            return null;
        }
        
        return NotificationAudit.builder()
            .withId(entity.getUuid())
            .withNotificationId(entity.getNotificationId())
            .withAction(entity.getAction())
            .withStatus(entity.getStatus())
            .withChannel(entity.getChannel())
            .withDetails(entity.getDetails())
            .withTimestamp(entity.getTimestamp())
            .withUserId(entity.getUserId())
            .withRequesterId(entity.getRequesterId())
            .withSourceService(entity.getSourceService())
            .withIpAddress(entity.getIpAddress())
            .withUserAgent(entity.getUserAgent())
            .withDeviceInfo(entity.getDeviceInfo())
            .build();
    }

    public NotificationAuditEntity toEntity(NotificationAudit domain) {
        if (domain == null) {
            log.warn("toEntity - Received null domain");
            return null;
        }

        NotificationAuditEntity entity = new NotificationAuditEntity();
        entity.setUuid(domain.getId());
        entity.setNotificationId(domain.getNotificationId());
        entity.setAction(domain.getAction());
        entity.setStatus(domain.getStatus());
        entity.setChannel(domain.getChannel());
        entity.setDetails(domain.getDetails());
        entity.setTimestamp(domain.getTimestamp());
        entity.setUserId(domain.getUserId());
        entity.setRequesterId(domain.getRequesterId());
        entity.setSourceService(domain.getSourceService());
        entity.setIpAddress(domain.getIpAddress());
        entity.setUserAgent(domain.getUserAgent());
        entity.setDeviceInfo(domain.getDeviceInfo());
        
        return entity;
    }
}