package edu.market.notification.infrastructure.adapter.output.persistence.repository;

import edu.market.notification.domain.enums.NotificationChannelType;
import edu.market.notification.infrastructure.adapter.output.persistence.entity.TemplateEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repositorio JPA para la entidad TemplateEntity.
 */
@Repository
public interface JpaTemplateRepository extends JpaRepository<TemplateEntity, UUID> {
    
    /**
     * Busca una plantilla por su UUID
     * @param uuid UUID de la plantilla
     * @return Plantilla encontrada o vacío
     */
    Optional<TemplateEntity> findByUuid(UUID uuid);
    
    /**
     * Busca una plantilla por su código
     * @param code Código de la plantilla
     * @return Plantilla encontrada o vacío
     */
    Optional<TemplateEntity> findByCode(String code);
    
    /**
     * Busca una plantilla por su código y versión
     * @param code Código de la plantilla
     * @param version Versión de la plantilla
     * @return Plantilla encontrada o vacío
     */
    Optional<TemplateEntity> findByCodeAndVersion(String code, Integer version);
    
    /**
     * Busca la última versión de una plantilla por su código
     * @param code Código de la plantilla
     * @return Plantilla encontrada o vacío
     */
    @Query("SELECT t FROM TemplateEntity t WHERE t.code = :code AND t.active = true ORDER BY t.version DESC")
    Optional<TemplateEntity> findLatestVersionByCode(@Param("code") String code);
    
    /**
     * Busca plantillas por canal soportado
     * @param channel Canal de comunicación
     * @return Lista de plantillas
     */
    @Query("SELECT t FROM TemplateEntity t JOIN t.supportedChannelEntities sc WHERE sc.channel = :channel")
    List<TemplateEntity> findBySupportedChannel(@Param("channel") NotificationChannelType channel);
    
    /**
     * Busca plantillas activas
     * @return Lista de plantillas activas
     */
    List<TemplateEntity> findByActiveTrue();
    
    /**
     * Busca plantillas por código y estado activo
     * @param code Código de la plantilla
     * @param active Estado activo
     * @return Lista de plantillas
     */
    List<TemplateEntity> findByCodeAndActive(String code, boolean active);
    
    /**
     * Elimina una plantilla por su UUID
     * @param uuid UUID de la plantilla
     */
    void deleteByUuid(UUID uuid);
}
