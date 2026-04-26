package edu.market.userservice.infrastructure.adapter.output.persistence.mapper;

import edu.market.userservice.domain.model.Role;
import edu.market.userservice.infrastructure.adapter.output.persistence.entity.RoleEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper para convertir entre entidades de dominio Role y entidades de persistencia RoleEntity
 */
@Slf4j
@Component
public class RoleMapper {

    /**
     * Convierte una entidad de dominio Role a una entidad de persistencia RoleEntity
     * 
     * @param domainModel Modelo de dominio de rol
     * @return Entidad de rol
     */
    public RoleEntity toEntity(Role domainModel) {
        log.debug("init - toEntity para role ID: {}", domainModel != null ? domainModel.getId() : "null");
        if (domainModel == null) {
            return null;
        }
        
        RoleEntity entity = new RoleEntity();
        entity.setId(domainModel.getId());
        entity.setName(domainModel.getName());
        entity.setDescription(domainModel.getDescription());
        
        log.debug("end - toEntity para role ID: {}", domainModel != null ? domainModel.getId() : "null");
        return entity;
    }
    
    /**
     * Convierte una entidad de persistencia RoleEntity a una entidad de dominio Role
     * 
     * @param entity Entidad de rol
     * @return Modelo de dominio de rol
     */
    public Role toDomain(RoleEntity entity) {
        log.debug("init - toDomain para role entity ID: {}", entity != null ? entity.getId() : "null");
        if (entity == null) {
            return null;
        }
        
        Role role = new Role();
        role.setId(entity.getId());
        role.setName(entity.getName());
        role.setDescription(entity.getDescription());
        
        log.debug("end - toDomain para role entity ID: {}", entity != null ? entity.getId() : "null");
        return role;
    }
    
    /**
     * Convierte una lista de entidades de persistencia RoleEntity a una lista de entidades de dominio Role
     * 
     * @param entities Lista de entidades de rol
     * @return Lista de modelos de dominio de rol
     */
    public List<Role> toDomainList(List<RoleEntity> entities) {
        log.debug("init - toDomainList para {} entidades", entities != null ? entities.size() : 0);
        if (entities == null) {
            return Collections.emptyList();
        }
        
        List<Role> result = entities.stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
        
        log.debug("end - toDomainList, convertidas {} entidades", result.size());
        return result;
    }
}
