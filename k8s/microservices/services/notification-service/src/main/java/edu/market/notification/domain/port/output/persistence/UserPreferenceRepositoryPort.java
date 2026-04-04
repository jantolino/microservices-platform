package edu.market.notification.domain.port.output.persistence;

import edu.market.notification.domain.model.UserPreference;

import java.util.Optional;
import java.util.UUID;

/**
 * Puerto de salida para la persistencia de preferencias de usuario.
 * Siguiendo el patrón de Ports and Adapters (Arquitectura Hexagonal).
 */
public interface UserPreferenceRepositoryPort {
    
    /**
     * Guarda las preferencias de un usuario
     * @param userPreference Preferencias a guardar
     * @return Preferencias guardadas
     */
    UserPreference save(UserPreference userPreference);
    
    /**
     * Busca las preferencias de un usuario por su ID
     * @param id ID de las preferencias
     * @return Preferencias encontradas o vacío
     */
    Optional<UserPreference> findById(UUID id);
    
    /**
     * Busca las preferencias por ID de usuario
     * @param userId ID del usuario
     * @return Preferencias encontradas o vacío
     */
    Optional<UserPreference> findByUserId(UUID userId);
    
    /**
     * Elimina las preferencias de un usuario
     * @param id ID de las preferencias
     */
    void deleteById(UUID id);
}
