package edu.market.notification.application.dto.request;

import java.util.Map;

import jakarta.validation.constraints.NotBlank;

/**
 * Comando para actualizar una plantilla de notificación existente.
 *
 * Estas anotaciones de validación son estándares de Java (JSR 303/380) y no introducen una dependencia
 * de un framework específico de infraestructura.
 *
 * Las validaciones se aplican en la capa de aplicación antes de que los datos lleguen al dominio,
 * actuando como un filtro inicial que protege la integridad del modelo de dominio.
 */
public record UpdateTemplateCommand(
    
    @NotBlank(message = "userUuid required")
    String userUuid,
    
    @NotBlank(message = "templateId required")
    String templateId,
    
    @NotBlank(message = "templateType required")
    String templateType,
    
    @NotBlank(message = "name required")
    String name,
    
    @NotBlank(message = "subject required")
    String subject,
    
    @NotBlank(message = "content required")
    String content,
    
    @NotBlank(message = "contentType required")
    String contentType,
    
    Map<String, Object> metadata
) {}
