package edu.market.notification.application.usecase.template;

import edu.market.notification.application.dto.request.ClientContextCommand;
import edu.market.notification.application.dto.request.UpdateTemplateCommand;
import edu.market.notification.application.dto.response.TemplateResponse;
import edu.market.notification.application.exception.ApplicationException;
import edu.market.notification.application.mapper.TemplateMapper;
import edu.market.notification.application.port.input.template.UpdateTemplateUseCasePort;
import edu.market.notification.domain.model.Template;
import edu.market.notification.domain.port.output.monitoring.LoggingPort;
import edu.market.notification.domain.port.output.persistence.TemplateRepositoryPort;

import java.util.Optional;
import java.util.UUID;

/**
 * Implementación del caso de uso para actualizar plantillas de notificaciones.
 * Sigue los principios de arquitectura hexagonal, manteniendo la lógica de negocio
 * independiente de la infraestructura.
 */
public class UpdateTemplateUseCase implements UpdateTemplateUseCasePort {

    private final TemplateRepositoryPort templateRepository;
    private final LoggingPort log;

    /**
     * Constructor para inyección de dependencias.
     * 
     * @param templateRepository Repositorio de plantillas
     * @param log Servicio de logging
     */
    public UpdateTemplateUseCase(TemplateRepositoryPort templateRepository, LoggingPort log) {
        this.templateRepository = templateRepository;
        this.log = log;
    }

    /**
     * Actualiza una plantilla de notificación existente.
     * 
     * @param command Comando con los datos actualizados de la plantilla
     * @return Respuesta con la plantilla actualizada
     * @throws ApplicationException Si ocurre algún error durante el proceso de actualización
     */
    @Override
    public TemplateResponse update(UpdateTemplateCommand command, ClientContextCommand clientContext) {
        log.info("update - Init with template ID: {}", command.templateId());
        
        try {
            // Validar el formato del ID
            UUID id;
            try {
                id = UUID.fromString(command.templateId());
            } catch (IllegalArgumentException e) {
                String errorMessage = "Invalid template ID format: " + command.templateId();
                log.warn("update - {}", errorMessage);
                throw new ApplicationException(errorMessage);
            }
            
            // Verificar si la plantilla existe
            log.debug("update - Checking if template exists");
            Optional<Template> templateOpt = templateRepository.findById(id);
            
            if (templateOpt.isEmpty()) {
                String errorMessage = "Template with ID " + command.templateId() + " not found";
                log.warn("update - {}", errorMessage);
                throw new ApplicationException(errorMessage);
            }
            
            Template existingTemplate = templateOpt.get();
            log.debug("update - Found existing template: {}", existingTemplate.getCode());
            
            // Crear una nueva versión de la plantilla con los datos actualizados usando el mapper
            log.debug("update - Creating updated template using mapper");
            Template updatedTemplate = TemplateMapper.createUpdatedVersion(existingTemplate, command);
            
            // Guardar la plantilla actualizada
            log.debug("update - Saving updated template");
            Template savedTemplate = templateRepository.save(updatedTemplate);
            
            // Convertir la entidad actualizada a un objeto de respuesta
            TemplateResponse response = TemplateMapper.toResponse(savedTemplate);
            
            log.info("update - End (template updated successfully with new version: {})", savedTemplate.getVersion());
            return response;
            
        } catch (ApplicationException ae) {
            // Propagar excepciones de aplicación
            throw ae;
        } catch (Exception e) {
            // Convertir otras excepciones en ApplicationException
            String errorMessage = "Error updating template: " + e.getMessage();
            log.error("update - Exception occurred", e);
            throw new ApplicationException(errorMessage, e);
        }
    }
}
