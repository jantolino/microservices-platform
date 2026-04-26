package edu.market.notification.application.usecase.template;

import edu.market.notification.application.dto.request.ClientContextCommand;
import edu.market.notification.application.dto.request.GetTemplatesQuery;
import edu.market.notification.application.dto.response.TemplateResponse;
import edu.market.notification.application.exception.ApplicationException;
import edu.market.notification.application.mapper.TemplateMapper;
import edu.market.notification.application.port.input.template.GetTemplateUseCasePort;
import edu.market.notification.domain.model.Template;
import edu.market.notification.domain.port.output.monitoring.LoggingPort;
import edu.market.notification.domain.port.output.persistence.TemplateRepositoryPort;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Implementación del caso de uso para obtener plantillas de notificaciones.
 * Sigue los principios de arquitectura hexagonal, manteniendo la lógica de negocio
 * independiente de la infraestructura.
 */
public class GetTemplateUseCase implements GetTemplateUseCasePort {

    private final TemplateRepositoryPort templateRepository;
    private final LoggingPort log;

    public GetTemplateUseCase(TemplateRepositoryPort templateRepository, LoggingPort log) {
        this.templateRepository = templateRepository;
        this.log = log;
    }

    /**
     * Recupera plantillas de notificaciones según los criterios especificados.
     * 
     * @param query Consulta con los criterios de búsqueda para las plantillas
     * @return Una lista de respuestas con las plantillas que coinciden con los criterios, o una lista vacía si no se encuentran
     */
    @Override
    public List<TemplateResponse> getTemplates(GetTemplatesQuery query, ClientContextCommand clientContext) {
        log.info("getTemplates - Init with query: templateId={}, templateType={}", 
                query.templateId(), query.templateType());
        
        try {
            List<Template> templates;
            
            // Buscar por ID específico
            if (query.templateId() != null && !query.templateId().isBlank()) {
                try {
                    // Intentar interpretar el ID como UUID
                    UUID templateId = UUID.fromString(query.templateId());
                    Optional<Template> templateOpt = templateRepository.findById(templateId);
                    
                    if (templateOpt.isPresent()) {
                        log.debug("getTemplates - Found template by ID: {}", templateId);
                        templates = Collections.singletonList(templateOpt.get());
                    } else {
                        log.debug("getTemplates - No template found with ID: {}", templateId);
                        templates = Collections.emptyList();
                    }
                } catch (IllegalArgumentException e) {
                    // Si no es un UUID válido, intentar buscar por código
                    log.debug("getTemplates - ID is not a valid UUID, trying as template code: {}", query.templateId());
                    Optional<Template> templateOpt = templateRepository.findLatestVersionByCode(query.templateId());
                    
                    if (templateOpt.isPresent()) {
                        log.debug("getTemplates - Found template by code: {}", query.templateId());
                        templates = Collections.singletonList(templateOpt.get());
                    } else {
                        log.debug("getTemplates - No template found with code: {}", query.templateId());
                        templates = Collections.emptyList();
                    }
                }
            }
            // Buscar por tipo de plantilla
            else if (query.templateType() != null && !query.templateType().isBlank()) {
                log.debug("getTemplates - Searching templates by type: {}", query.templateType());
                // Aquí asumimos que el tipo de plantilla está relacionado con el código
                // En una implementación real, podría haber un método específico en el repositorio
                templates = templateRepository.findAllActive().stream()
                        .filter(t -> t.getCode().startsWith(query.templateType()))
                        .collect(Collectors.toList());
                log.debug("getTemplates - Found {} templates matching type: {}", templates.size(), query.templateType());
            }
            // Sin criterios específicos, devolver todas las plantillas activas
            else {
                log.debug("getTemplates - No specific criteria, returning all active templates");
                templates = templateRepository.findAllActive();
                log.debug("getTemplates - Found {} active templates", templates.size());
            }
            
            // Convertir las entidades de dominio a objetos de respuesta
            List<TemplateResponse> responses = templates.stream()
                    .map(TemplateMapper::toResponse)
                    .collect(Collectors.toList());
            
            log.info("getTemplates - End (found {} templates)", responses.size());
            return responses;
        } catch (Exception e) {
            String errorMessage = "Error retrieving templates: " + e.getMessage();
            log.error("getTemplates - Exception occurred", e);
            throw new ApplicationException(errorMessage, e);
        }
    }
}
