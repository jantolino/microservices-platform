package edu.market.userservice.infrastructure.adapter.output.persistence.mapper;

import edu.market.userservice.domain.enums.TokenStatusType;
import edu.market.userservice.domain.model.UserAuthToken;
import edu.market.userservice.infrastructure.adapter.output.persistence.entity.UserAuthTokenEntity;
import edu.market.userservice.infrastructure.adapter.output.persistence.entity.UserEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper para convertir entre entidades de persistencia y modelos de dominio de tokens
 */
@Slf4j
@Component
public class TokenMapper {
    
    /**
     * Convierte una entidad de token a un modelo de dominio
     * 
     * @param entity Entidad de token
     * @return Modelo de dominio de token
     */
    /**
     * Alias para toDomainModel para mantener compatibilidad con código existente
     * 
     * @param entity Entidad de token
     * @return Modelo de dominio de token
     */
    public UserAuthToken toDomain(UserAuthTokenEntity entity) {
        return toDomainModel(entity);
    }
    
    /**
     * Convierte una lista de entidades de token a una lista de modelos de dominio
     * 
     * @param entities Lista de entidades de token
     * @return Lista de modelos de dominio de token
     */
    public List<UserAuthToken> toDomainList(List<UserAuthTokenEntity> entities) {
        log.debug("init - toDomainList para {} tokens", entities != null ? entities.size() : "null");
        if (entities == null) {
            return List.of();
        }
        
        List<UserAuthToken> result = entities.stream()
                .map(this::toDomainModel)
                .collect(Collectors.toList());
                
        log.debug("end - toDomainList, convertidos {} tokens", result.size());
        return result;
    }
    
    public UserAuthToken toDomainModel(UserAuthTokenEntity entity) {
        log.debug("init - toDomainModel para token entity ID: {}", entity != null ? entity.getId() : "null");
        if (entity == null) {
            return null;
        }
        
        UserAuthToken token = new UserAuthToken();
        token.setId(entity.getId());
        token.setAccessToken(entity.getToken()); // Mapeo de token a accessToken
        token.setRefreshToken(entity.getRefreshToken());
        token.setExpiresAt(entity.getExpiresAt());
        token.setCreatedAt(entity.getCreatedAt());
        
        // Mapeo de status a TokenStatusType
        if (entity.getStatus() != null) {
            try {
                token.setStatus(TokenStatusType.valueOf(entity.getStatus()));
            } catch (IllegalArgumentException e) {
                // Si el valor no es válido, establecer como EXPIRED por defecto
                token.setStatus(TokenStatusType.EXPIRED);
            }
        }
        
        // Establecer el ID del usuario en lugar del objeto User completo
        if (entity.getUser() != null) {
            token.setUserId(entity.getUser().getId());
        }
        
        // Establecer otros campos disponibles
        token.setIpAddress(entity.getIpAddress());
        token.setUserAgent(entity.getDeviceInfo());
        if (entity.getProvider() != null) {
            token.setProvider(entity.getProvider());
        }
        
        log.debug("end - toDomainModel para token entity ID: {}", entity != null ? entity.getId() : "null");
        return token;
    }
    
    /**
     * Convierte un modelo de dominio de token a una entidad de persistencia
     * 
     * @param domainModel Modelo de dominio de token
     * @return Entidad de token
     */
    /**
     * Convierte un modelo de dominio de token a una entidad de persistencia
     * y establece el usuario asociado
     * 
     * @param domainModel Modelo de dominio de token
     * @param userEntity Entidad de usuario asociada al token
     * @return Entidad de token
     */
    public UserAuthTokenEntity toEntity(UserAuthToken domainModel, UserEntity userEntity) {
        log.debug("init - toEntity para token ID: {} con usuario ID: {}", 
                domainModel != null ? domainModel.getId() : "null", 
                userEntity != null ? userEntity.getId() : "null");
                
        if (domainModel == null) {
            return null;
        }
        
        UserAuthTokenEntity entity = toEntity(domainModel);
        entity.setUser(userEntity);
        
        log.debug("end - toEntity para token ID: {} con usuario ID: {}", 
                entity.getId(), userEntity != null ? userEntity.getId() : "null");
        return entity;
    }
    
    public UserAuthTokenEntity toEntity(UserAuthToken domainModel) {
        log.debug("init - toEntity para token ID: {}", domainModel != null ? domainModel.getId() : "null");
        if (domainModel == null) {
            return null;
        }
        
        UserAuthTokenEntity entity = new UserAuthTokenEntity();
        entity.setId(domainModel.getId());
        entity.setToken(domainModel.getAccessToken()); // Mapeo de accessToken a token
        entity.setRefreshToken(domainModel.getRefreshToken());
        entity.setExpiresAt(domainModel.getExpiresAt());
        entity.setCreatedAt(domainModel.getCreatedAt());
        
        // Mapeo de TokenStatusType a status (String)
        if (domainModel.getStatus() != null) {
            entity.setStatus(domainModel.getStatus().name());
        }
        
        // Establecer otros campos disponibles
        entity.setIpAddress(domainModel.getIpAddress());
        entity.setDeviceInfo(domainModel.getUserAgent());
        if (domainModel.getProvider() != null) {
            entity.setProvider(domainModel.getProvider());
        }       
        
        log.debug("end - toEntity para token ID: {}", domainModel != null ? domainModel.getId() : "null");
        return entity;
    }
}
