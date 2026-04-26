package edu.market.notification.infrastructure.adapter.output.persistence.repository;

import edu.market.notification.infrastructure.adapter.output.persistence.entity.UserPreferenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repositorio JPA para la entidad UserPreferenceEntity.
 */
@Repository
public interface JpaUserPreferenceRepository extends JpaRepository<UserPreferenceEntity, UUID> {
    
    /**
     * Busca una preferencia de usuario por su UUID
     * @param uuid UUID de la preferencia
     * @return Preferencia encontrada o vacío
     */
    Optional<UserPreferenceEntity> findByUuid(UUID uuid);
    
    /**
     * Busca una preferencia de usuario por el ID del usuario
     * @param userId ID del usuario
     * @return Preferencia encontrada o vacío
     */
    Optional<UserPreferenceEntity> findByUserId(UUID userId);
}
