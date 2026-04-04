package edu.market.userservice.infrastructure.adapter.output.persistence.mapper;

import edu.market.userservice.domain.model.Role;
import edu.market.userservice.domain.model.User;
import edu.market.userservice.infrastructure.adapter.output.persistence.entity.RoleEntity;
import edu.market.userservice.infrastructure.adapter.output.persistence.entity.UserEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper para convertir entre entidades de persistencia y modelos de dominio de usuario
 */
@Slf4j
@Component
public class UserMapper {
    
    /**
     * Convierte una entidad de usuario a un modelo de dominio
     * 
     * @param entity Entidad de usuario
     * @return Modelo de dominio de usuario
     */
    public User toDomain(UserEntity entity) {
        log.debug("init - toDomain para user entity ID: {}", entity != null ? entity.getId() : "null");
        if (entity == null) {
            return null;
        }
        
        User user = new User();
        user.setId(entity.getId());
        user.setEmail(entity.getEmail());
        user.setPassword(entity.getPassword());
        user.setFirstName(entity.getFirstName());
        user.setLastName(entity.getLastName());
        user.setEnabled(entity.isEnabled());
        user.setCreatedAt(entity.getCreatedAt());
        user.setUpdatedAt(entity.getUpdatedAt());
        user.setLastLoginAt(entity.getLastLoginAt());
        
        if (entity.getUserRoles() != null) {
            List<Role> roles = entity.getUserRoles()
                    .stream()
                    .map(userRole -> toDomain(userRole.getRole()))
                    .collect(Collectors.toList());
            user.setRoles(roles);
        }
        
        log.debug("end - toDomain para user entity ID: {}", entity != null ? entity.getId() : "null");
        return user;
    }
    
    /**
     * Convierte un modelo de dominio de usuario a una entidad de persistencia
     * 
     * @param domainModel Modelo de dominio de usuario
     * @return Entidad de usuario
     */
    public UserEntity toEntity(User domainModel) {
        log.debug("init - toEntity para user ID: {}", domainModel != null ? domainModel.getId() : "null");
        if (domainModel == null) {
            return null;
        }
        
        UserEntity entity = new UserEntity();
        entity.setId(domainModel.getId());
        entity.setEmail(domainModel.getEmail());
        entity.setPassword(domainModel.getPassword());
        entity.setFirstName(domainModel.getFirstName());
        entity.setLastName(domainModel.getLastName());
        entity.setEnabled(domainModel.isEnabled());
        entity.setCreatedAt(domainModel.getCreatedAt());
        entity.setUpdatedAt(domainModel.getUpdatedAt());
        entity.setLastLoginAt(domainModel.getLastLoginAt());
        entity.setName(domainModel.getName());
        
        if (domainModel.getName() == null) {
            entity.setName(domainModel.getFirstName() + " " + domainModel.getLastName());
        } else {            
            entity.setName(domainModel.getName());
        }
        
        return entity;
    }
    
    /**
     * Convierte una entidad de rol a un modelo de dominio
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
        
        log.debug("end - toDomain para role entity ID: {}", entity != null ? entity.getId() : "null");
        return role;
    }
    
    
    /**
     * Convierte un modelo de dominio de rol a una entidad de persistencia
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
        
        return entity;
    }
}
