package edu.market.notification.application.mapper;

import edu.market.notification.application.dto.request.UpdateTemplateCommand;
import edu.market.notification.application.dto.response.TemplateResponse;
import edu.market.notification.domain.model.Template;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Mapper para convertir entre entidades de dominio Template y objetos de valor TemplateResponse.
 * Siguiendo el patrón de arquitectura hexagonal, este mapper pertenece a la capa de aplicación
 * y se encarga de transformar objetos del dominio a objetos de la capa de aplicación.
 */
public class TemplateMapper {

    /**
     * Convierte una entidad Template a un objeto TemplateResponse
     * 
     * @param template La entidad de dominio a convertir
     * @return El objeto TemplateResponse correspondiente
     */
    public static TemplateResponse toResponse(Template template) {
        if (template == null) {
            return null;
        }
        
        // Crear un mapa de metadatos con información adicional
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("version", template.getVersion());
        metadata.put("active", template.isActive());
        metadata.put("supportedChannels", template.getSupportedChannels().stream()
                .map(Enum::name)
                .collect(Collectors.toList()));
        
        if (template.getSourceService() != null) {
            metadata.put("sourceService", template.getSourceService().name());
        }
        
        return new TemplateResponse(
            template.getId().toString(),
            template.getCode(),
            template.getName(),
            template.getSubject(),
            template.getContent(),
            "text/html", // Por defecto asumimos HTML, podría ser parametrizable en el futuro
            metadata,
            template.getCreatedAt(),
            template.getUpdatedAt()
        );
    }
    
    /**
     * Crea una versión actualizada de una plantilla existente
     * 
     * @param existingTemplate La plantilla existente a actualizar
     * @param command El comando con los datos actualizados
     * @return Una nueva instancia de Template con los datos actualizados
     */
    public static Template createUpdatedVersion(Template existingTemplate, UpdateTemplateCommand command) {
        return Template.builder()
            .withId(existingTemplate.getId())
            .withCode(existingTemplate.getCode())
            .withName(command.name() != null ? command.name() : existingTemplate.getName())
            .withSubject(command.subject() != null ? command.subject() : existingTemplate.getSubject())
            .withContent(command.content() != null ? command.content() : existingTemplate.getContent())
            // No hay getContentType() ni getMetadata() en Template, así que usamos los valores del comando directamente
            .withSupportedChannels(existingTemplate.getSupportedChannels())
            .withVersion(existingTemplate.getVersion() + 1) // Incrementar versión
            .withActive(existingTemplate.isActive())
            .withCreatedAt(existingTemplate.getCreatedAt())
            .withUpdatedAt(LocalDateTime.now())
            .withCreatedBy(existingTemplate.getCreatedBy())
            .withUpdatedBy(null) // Podría ser un parámetro adicional si se necesita
            .withSourceService(existingTemplate.getSourceService())
            .build();
    }
}
