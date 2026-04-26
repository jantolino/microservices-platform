package edu.market.notification.domain.port.output.persistence;

import edu.market.notification.domain.enums.NotificationChannelType;
import edu.market.notification.domain.model.Template;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Puerto de salida para la persistencia de plantillas.
 * Siguiendo el patrón de Ports and Adapters (Arquitectura Hexagonal).
 */
public interface TemplateRepositoryPort {
    
    /**
     * Guarda una plantilla
     * @param template Plantilla a guardar
     * @return Plantilla guardada
     */
    Template save(Template template);
    
    /**
     * Busca una plantilla por su ID
     * @param id ID de la plantilla
     * @return Plantilla encontrada o vacío
     */
    Optional<Template> findById(UUID id);
    
    /**
     * Busca una plantilla por su código
     * @param code Código de la plantilla
     * @param active Si se debe buscar solo plantillas activas
     * @return Plantilla encontrada o vacío
     */
    Optional<Template> findByCode(String code, boolean active);
    
    /**
     * Busca la última versión de una plantilla por su código
     * @param code Código de la plantilla
     * @return Plantilla encontrada o vacío
     */
    Optional<Template> findLatestVersionByCode(String code);
    
    /**
     * Verifica si existe una plantilla con el código especificado
     * @param code Código de la plantilla
     * @return true si existe, false en caso contrario
     */
    boolean existsByCode(String code);
    
    /**
     * Busca una versión específica de una plantilla por su código
     * @param code Código de la plantilla
     * @param version Versión de la plantilla
     * @return Plantilla encontrada o vacío
     */
    Optional<Template> findByCodeAndVersion(String code, int version);
    
    /**
     * Busca plantillas por canal soportado
     * @param channel Canal de notificación
     * @return Lista de plantillas que soportan el canal
     */
    List<Template> findBySupportedChannel(NotificationChannelType channel);
    
    /**
     * Busca todas las plantillas activas
     * @return Lista de plantillas activas
     */
    List<Template> findAllActive();
    
    /**
     * Busca todas las versiones de una plantilla por su código
     * @param code Código de la plantilla
     * @return Lista de versiones de la plantilla
     */
    List<Template> findAllVersionsByCode(String code);
    
    /**
     * Elimina una plantilla
     * @param id ID de la plantilla
     */
    void deleteById(UUID id);
}
