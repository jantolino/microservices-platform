package edu.market.notification.infrastructure.adapter.output.persistence.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.market.notification.domain.enums.NotificationChannelType;
import edu.market.notification.domain.model.Notification;
import edu.market.notification.domain.vo.NotificationContentVO;
import edu.market.notification.domain.vo.RecipientVO;
import edu.market.notification.infrastructure.adapter.output.persistence.entity.NotificationChannelEntity;
import edu.market.notification.infrastructure.adapter.output.persistence.entity.NotificationEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Mapper para convertir entre entidades de dominio Notification y entidades de persistencia NotificationEntity.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationOutputMapper {
    
    private final ObjectMapper objectMapper;

    /**
     * Convierte una entidad de persistencia a un objeto de dominio
     * @param entity Entidad de persistencia
     * @return Objeto de dominio
     */
    public Notification toDomain(NotificationEntity entity) {
        if (entity == null) {
            return null;
        }
        
        // Convertir canales de notificación
        Set<NotificationChannelType> channels = new HashSet<>();
        if (entity.getChannelEntities() != null) {
            channels = entity.getChannelEntities().stream()
                    .map(NotificationChannelEntity::getChannel)
                    .collect(Collectors.toSet());
        }
        
        // Convertir atributos de contenido desde JSON
        Map<String, Object> contentAttributes = parseJsonToMap(entity.getContentAttributes());
        
        // Crear objeto de contenido
        NotificationContentVO content = NotificationContentVO.of(
                entity.getContentBody(), 
                contentAttributes);
        
        // Convertir atributos del destinatario desde JSON
        Map<String, Object> recipientAttributes = parseJsonToMap(entity.getRecipientAttributes());
        
        // Crear objeto de destinatario usando el builder
        RecipientVO recipient = new RecipientVO.Builder()
                .withUserId(entity.getRecipientUserId())
                .withEmail(entity.getRecipientEmail())
                .withPhoneNumber(entity.getRecipientPhone())
                .withDeviceToken(entity.getRecipientDeviceToken())
                .withAttributes(recipientAttributes)
                .build();
        
        return Notification.builder()
                .withId(entity.getUuid())
                .withSubject(entity.getSubject())
                .withContent(content)
                .withRecipient(recipient)
                .withChannels(channels)
                .withPriority(entity.getPriority())
                .withStatus(entity.getStatus())
                .withCreatedAt(entity.getCreatedAt())
                .withScheduledFor(entity.getScheduledFor())
                .withSentAt(entity.getSentAt())
                .withRetryCount(entity.getRetryCount())
                .withErrorMessage(entity.getErrorMessage())
                .withTemplateId(entity.getTemplateId())
                .withRequesterId(entity.getRequesterId())
                .withSourceService(entity.getSourceService())
                .build();
    }

    /**
     * Convierte un objeto de dominio a una entidad de persistencia
     * @param domain Objeto de dominio
     * @return Entidad de persistencia
     */
    public NotificationEntity toEntity(Notification domain) {
        log.info("toEntity - Init");
        if (domain == null) {
            log.warn("toEntity - Received null notification");
            return null;
        }
        
        try {
            NotificationEntity entity = new NotificationEntity();
            entity.setUuid(domain.getId());
            entity.setSubject(domain.getSubject());
            
            // Mapear contenido
            NotificationContentVO content = domain.getContent();
            if (content != null) {
                entity.setContentBody(content.body());
                
                // Convertir atributos a JSON
                try {
                    if (!content.attributes().isEmpty()) {
                        entity.setContentAttributes(objectMapper.writeValueAsString(content.attributes()));
                    }
                } catch (JsonProcessingException e) {
                    log.error("toEntity - Failed to serialize content attributes: {}", e.getMessage());
                }
            }
            
            // Mapear destinatario
            RecipientVO recipient = domain.getRecipient();
            if (recipient != null) {
                entity.setRecipientUserId(recipient.userId());
                entity.setRecipientEmail(recipient.email());
                entity.setRecipientPhone(recipient.phoneNumber());
                entity.setRecipientDeviceToken(recipient.deviceToken());
                
                // Convertir atributos a JSON
                try {
                    if (!recipient.attributes().isEmpty()) {
                        entity.setRecipientAttributes(objectMapper.writeValueAsString(recipient.attributes()));
                    }
                } catch (JsonProcessingException e) {
                    log.error("toEntity - Failed to serialize recipient attributes: {}", e.getMessage());
                }
            }
            
            // Mapear canales como entidades relacionadas
            if (domain.getChannels() != null && !domain.getChannels().isEmpty()) {
                Set<NotificationChannelEntity> channelEntities = domain.getChannels().stream()
                        .map(channel -> {
                            NotificationChannelEntity channelEntity = new NotificationChannelEntity();
                            channelEntity.setNotification(entity);
                            channelEntity.setChannel(channel);
                            return channelEntity;
                        })
                        .collect(Collectors.toSet());
                entity.setChannelEntities(channelEntities);
            }
            
            // Mapear el resto de campos
            entity.setPriority(domain.getPriority());
            entity.setStatus(domain.getStatus());
            entity.setCreatedAt(domain.getCreatedAt());
            entity.setScheduledFor(domain.getScheduledFor());
            entity.setSentAt(domain.getSentAt());
            entity.setRetryCount(domain.getRetryCount());
            entity.setErrorMessage(domain.getErrorMessage());
            entity.setTemplateId(domain.getTemplateId());
            entity.setRequesterId(domain.getRequesterId());
            entity.setSourceService(domain.getSourceService());
            
            log.info("toEntity - End");
            return entity;
        } catch (Exception e) {
            log.error("toEntity - Notification mapping failed: {}", e.getMessage());
            throw e;
        }
    }
    
    /**
     * Convierte un string JSON a un Map
     * @param json String JSON
     * @return Map con los atributos
     */
    private Map<String, Object> parseJsonToMap(String json) {
        if (json == null || json.isBlank()) {
            return Collections.emptyMap();
        }
        
        try {
            return objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {});
        } catch (JsonProcessingException e) {
            log.error("parseJsonToMap error - JSON deserialization failed | exception: {}", e.getMessage());
            return Collections.emptyMap();
        }
    }
}
