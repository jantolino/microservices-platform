package edu.market.notification.application.usecase.template;

import edu.market.notification.application.dto.request.ClientContextCommand;
import edu.market.notification.application.dto.request.CreateTemplateCommand;
import edu.market.notification.application.dto.response.TemplateResponse;
import edu.market.notification.application.exception.ApplicationException;
import edu.market.notification.application.mapper.TemplateMapper;
import edu.market.notification.application.port.input.template.CreateTemplateUseCasePort;
import edu.market.notification.domain.model.Template;
import edu.market.notification.domain.port.output.monitoring.LoggingPort;
import edu.market.notification.domain.port.output.persistence.TemplateRepositoryPort;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Implementación del caso de uso para crear plantillas de notificaciones.
 * Sigue los principios de arquitectura hexagonal, manteniendo la lógica de negocio
 * independiente de la infraestructura.
 */
public class CreateTemplateUseCase implements CreateTemplateUseCasePort {

    private final TemplateRepositoryPort templateRepository;
    private final LoggingPort log;

    /**
     * Constructor para inyección de dependencias.
     * 
     * @param templateRepository Repositorio de plantillas
     * @param log Servicio de logging
     */
    public CreateTemplateUseCase(TemplateRepositoryPort templateRepository, LoggingPort log) {
        this.templateRepository = templateRepository;
        this.log = log;
    }

    /**
     * Crea una nueva plantilla de notificación.
     * 
     * @param command Comando con los datos de la plantilla a crear
     * @return Respuesta con la plantilla creada y su identificador asignado
     * @throws ApplicationException Si ocurre algún error durante el proceso de creación
     */
    @Override
    public TemplateResponse create(CreateTemplateCommand command, ClientContextCommand clientContext) {
        
        log.info("create - Init with template type: {}, name: {}", command.templateType(), command.name());
        
        try {
            // Validar si ya existe una plantilla con el mismo código
            log.debug("create - Call generateTemplateCode");
            String templateCode = generateTemplateCode(command.templateType(), command.name());
            
            log.debug("create - existsByCode");
            if (templateRepository.existsByCode(templateCode)) {
                String errorMessage = "Template with code " + templateCode + " already exists";
                log.warn("create - %s", errorMessage);
                throw new ApplicationException(errorMessage);
            }
            
            // Crear la nueva entidad de plantilla
            log.debug("create - Creating new template entity");
            
            Template template = Template.builder()
                .withId(UUID.randomUUID())
                .withCode(templateCode)
                .withName(command.name())
                .withSubject(command.subject())
                .withContent(command.content())
                .withVersion(1) // Primera versión
                .withActive(true)
                .withCreatedAt(LocalDateTime.now())
                .build();
            
            // Guardar la plantilla en el repositorio
            log.debug("create - Call templateRepository.save");
            Template savedTemplate = templateRepository.save(template);
            
            // Convertir la entidad guardada a un objeto de respuesta
            TemplateResponse response = TemplateMapper.toResponse(savedTemplate);
            
            log.info("create - End");
            return response;
            
        } catch (Exception e) {
            // Convertir otras excepciones en ApplicationException
            String errorMessage = "Error creating template: " + e.getMessage();
            log.error("create - Exception occurred", e);
            throw new ApplicationException(errorMessage, e);
        }
    }
    
    /**
     * Genera un código único para la plantilla basado en el tipo y nombre.
     * 
     * @param templateType Tipo de plantilla
     * @param name Nombre de la plantilla
     * @return Código único para la plantilla
     */
    private String generateTemplateCode(String templateType, String name) {
        // Normalizar el tipo y nombre para generar un código consistente
        String normalizedType = templateType.toUpperCase().trim().replace(" ", "_");
        String normalizedName = name != null ? name.toUpperCase().trim().replace(" ", "_") : "";
        
        return normalizedType + (normalizedName.isEmpty() ? "" : "_" + normalizedName);
    }
}
