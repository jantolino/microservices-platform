package edu.market.notification.application.usecase.template;

import edu.market.notification.application.dto.request.ClientContextCommand;
import edu.market.notification.application.exception.ApplicationException;
import edu.market.notification.application.port.input.template.DeleteTemplateUseCasePort;
import edu.market.notification.domain.model.Template;
import edu.market.notification.domain.port.output.monitoring.LoggingPort;
import edu.market.notification.domain.port.output.persistence.TemplateRepositoryPort;

import java.util.Optional;
import java.util.UUID;

/**
 * Implementación del caso de uso para eliminar plantillas de notificaciones.
 * Sigue los principios de arquitectura hexagonal, manteniendo la lógica de negocio
 * independiente de la infraestructura.
 */
public class DeleteTemplateUseCase implements DeleteTemplateUseCasePort {

    private final TemplateRepositoryPort templateRepository;
    private final LoggingPort log;

    /**
     * Constructor para inyección de dependencias.
     * 
     * @param templateRepository Repositorio de plantillas
     * @param log Servicio de logging
     */
    public DeleteTemplateUseCase(TemplateRepositoryPort templateRepository, LoggingPort log) {
        this.templateRepository = templateRepository;
        this.log = log;
    }

    /**
     * Elimina una plantilla de notificación existente.
     * 
     * @param templateId El identificador de la plantilla a eliminar
     * @return true si la plantilla fue eliminada correctamente, false en caso contrario
     * @throws ApplicationException Si ocurre algún error durante el proceso de eliminación
     */
    @Override
    public boolean delete(String templateId, ClientContextCommand clientContext) {
        log.info("delete - Init with template ID: {}", templateId);
        
        try {
            // Validar el formato del ID
            UUID id;
            try {
                id = UUID.fromString(templateId);
            } catch (IllegalArgumentException e) {
                String errorMessage = "Invalid template ID format: " + templateId;
                log.warn("delete - {}", errorMessage);
                throw new ApplicationException(errorMessage);
            }
            
            // Verificar si la plantilla existe
            log.debug("delete - Checking if template exists");
            Optional<Template> templateOpt = templateRepository.findById(id);
            
            if (templateOpt.isEmpty()) {
                log.warn("delete - Template with ID {} not found", templateId);
                return false;
            } else {
                // Eliminar la plantilla
                log.debug("delete - Deleting template with ID: {}", templateId);
                templateRepository.deleteById(id);
            }
            
            log.info("delete - End");
            return true;
            
        } catch (Exception e) {
            // Convertir otras excepciones en ApplicationException
            String errorMessage = "Error deleting template: " + e.getMessage();
            log.error("delete - Exception occurred", e);
            throw new ApplicationException(errorMessage, e);
        }
    }
}
