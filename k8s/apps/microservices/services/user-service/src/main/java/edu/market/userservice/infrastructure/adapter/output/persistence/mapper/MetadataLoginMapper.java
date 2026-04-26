package edu.market.userservice.infrastructure.adapter.output.persistence.mapper;

import edu.market.userservice.domain.model.MetadataLogin;
import edu.market.userservice.infrastructure.adapter.output.persistence.entity.MetadataLoginEntity;
import edu.market.userservice.infrastructure.adapter.output.persistence.entity.UserEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper para convertir entre entidades de dominio MetadataLogin y entidades de persistencia MetadataLoginEntity
 */
@Slf4j
@Component
public class MetadataLoginMapper {

    /**
     * Convierte una entidad de dominio MetadataLogin a una entidad de persistencia MetadataLoginEntity
     * 
     * @param metadataLogin Entidad de dominio MetadataLogin
     * @param userEntity Entidad de persistencia UserEntity asociada al metadata
     * @return Entidad de persistencia MetadataLoginEntity
     */
    public MetadataLoginEntity toEntity(MetadataLogin metadataLogin, UserEntity userEntity) {
        log.debug("init - toEntity para metadataLogin ID: {}", metadataLogin != null ? metadataLogin.getId() : "null");
        
        if (metadataLogin == null) {
            return null;
        }
        
        MetadataLoginEntity entity = new MetadataLoginEntity();
        entity.setId(metadataLogin.getId());
        entity.setProvider(metadataLogin.getProvider());
        entity.setProviderId(metadataLogin.getProviderId());
        entity.setPictureUrl(metadataLogin.getPictureUrl());
        entity.setEnabled(metadataLogin.isEnabled());
        entity.setIpAddress(metadataLogin.getIpAddress());
        entity.setUserAgent(metadataLogin.getUserAgent());
        entity.setDeviceInfo(metadataLogin.getDeviceInfo());
        entity.setCreatedAt(metadataLogin.getCreatedAt());
        entity.setUpdatedAt(metadataLogin.getUpdatedAt());
        entity.setUser(userEntity);
        
        log.debug("end - toEntity para metadataLogin ID: {}", metadataLogin != null ? metadataLogin.getId() : "null");
        return entity;
    }
    
    /**
     * Convierte una entidad de persistencia MetadataLoginEntity a una entidad de dominio MetadataLogin
     * 
     * @param entity Entidad de persistencia MetadataLoginEntity
     * @return Entidad de dominio MetadataLogin
     */
    public MetadataLogin toDomain(MetadataLoginEntity entity) {
        log.debug("init - toDomain para metadataLogin entity ID: {}", entity != null ? entity.getId() : "null");
        
        if (entity == null) {
            return null;
        }
        
        MetadataLogin metadataLogin = new MetadataLogin();
        metadataLogin.setId(entity.getId());
        
        if (entity.getUser() != null) {
            metadataLogin.setUserId(entity.getUser().getId());
        }
        
        metadataLogin.setProvider(entity.getProvider());
        metadataLogin.setProviderId(entity.getProviderId());
        metadataLogin.setPictureUrl(entity.getPictureUrl());
        metadataLogin.setEnabled(entity.getEnabled());
        metadataLogin.setIpAddress(entity.getIpAddress());
        metadataLogin.setUserAgent(entity.getUserAgent());
        metadataLogin.setDeviceInfo(entity.getDeviceInfo());
        metadataLogin.setCreatedAt(entity.getCreatedAt());
        metadataLogin.setUpdatedAt(entity.getUpdatedAt());
        
        log.debug("end - toDomain para metadataLogin entity ID: {}", entity != null ? entity.getId() : "null");
        return metadataLogin;
    }
    
    /**
     * Convierte una lista de entidades de persistencia MetadataLoginEntity a una lista de entidades de dominio MetadataLogin
     * 
     * @param entities Lista de entidades de persistencia MetadataLoginEntity
     * @return Lista de entidades de dominio MetadataLogin
     */
    public List<MetadataLogin> toDomainList(List<MetadataLoginEntity> entities) {
        log.debug("init - toDomainList para {} entidades", entities != null ? entities.size() : 0);
        
        if (entities == null) {
            return Collections.emptyList();
        }
        
        List<MetadataLogin> result = entities.stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
        
        log.debug("end - toDomainList, convertidas {} entidades", result.size());
        return result;
    }
}
