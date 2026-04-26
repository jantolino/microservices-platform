package edu.market.userservice.domain.port.output;

import edu.market.userservice.domain.model.Role;

import java.util.List;
import java.util.Optional;

/**
 * Puerto para operaciones de repositorio de roles
 */
public interface RoleRepositoryPort {
    
    /**
     * Guarda un rol
     * @param role Rol a guardar
     * @return Rol guardado
     */
    Role save(Role role);
    
    /**
     * Busca un rol por su ID
     * @param id ID del rol
     * @return Rol encontrado o vacío
     */
    Optional<Role> findById(Long id);
    
    /**
     * Busca un rol por su nombre
     * @param name Nombre del rol
     * @return Rol encontrado o vacío
     */
    Optional<Role> findByName(String name);
    
    /**
     * Obtiene todos los roles
     * @return Lista de roles
     */
    List<Role> findAll();
}
