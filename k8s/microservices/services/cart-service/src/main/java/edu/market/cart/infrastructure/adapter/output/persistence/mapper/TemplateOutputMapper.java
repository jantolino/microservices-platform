package edu.market.notification.infrastructure.adapter.output.persistence.mapper;

import edu.market.notification.domain.enums.NotificationChannelType;
import edu.market.notification.domain.model.Template;
import edu.market.notification.infrastructure.adapter.output.persistence.entity.TemplateEntity;
import edu.market.notification.infrastructure.adapter.output.persistence.entity.TemplateSupportedChannelEntity;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Mapper para convertir entre entidades de dominio Template y entidades de persistencia TemplateEntity.
 */
@Component
public class TemplateOutputMapper {

    /**
     * Convierte una entidad de persistencia a un objeto de dominio
     * @param entity Entidad de persistencia
     * @return Objeto de dominio
     */
    public Template toDomain(TemplateEntity entity) {
        if (entity == null) {
            return null;
        }
        
        // Convertir los canales soportados
        Set<NotificationChannelType> supportedChannels = entity.getSupportedChannelEntities().stream()
                .map(channel -> channel.getChannel())
                .collect(Collectors.toSet());
        
        return Template.builder()
                .withId(entity.getUuid())
                .withCode(entity.getCode())
                .withName(entity.getName())
                .withContent(entity.getContent())
                .withSubject(entity.getSubject())
                .withLanguage(entity.getLanguage())
                .withVersion(entity.getVersion())
                .withActive(entity.getActive())
                .withSupportedChannels(supportedChannels)
                .withCreatedAt(entity.getCreatedAt())
                .withUpdatedAt(entity.getUpdatedAt())
                .withSourceService(entity.getSourceService())
                .build();
    }

    /**
     * Convierte un objeto de dominio a una entidad de persistencia
     * @param domain Objeto de dominio
     * @return Entidad de persistencia
     */
    public TemplateEntity toEntity(Template domain) {
        if (domain == null) {
            return null;
        }
        
        TemplateEntity entity = new TemplateEntity();
        entity.setUuid(domain.getId());
        entity.setCode(domain.getCode());
        entity.setName(domain.getName());
        entity.setContent(domain.getContent());
        entity.setSubject(domain.getSubject());
        entity.setLanguage(domain.getLanguage());
        entity.setVersion(domain.getVersion());
        entity.setActive(domain.isActive());
        entity.setCreatedAt(domain.getCreatedAt());
        entity.setUpdatedAt(domain.getUpdatedAt());
        entity.setSourceService(domain.getSourceService());
        
        // Convertir los canales soportados
        if (domain.getSupportedChannels() != null) {
            Set<TemplateSupportedChannelEntity> supportedChannels = domain.getSupportedChannels().stream()
                    .map(channel -> {
                        TemplateSupportedChannelEntity channelEntity = new TemplateSupportedChannelEntity();
                        channelEntity.setTemplate(entity); // Usar la referencia a la entidad directamente
                        channelEntity.setChannel(channel);
                        return channelEntity;
                    })
                    .collect(Collectors.toSet());
            entity.setSupportedChannelEntities(supportedChannels);
        }
        
        return entity;
    }
}
