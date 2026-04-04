package edu.market.notification.application.dto.request;

import jakarta.validation.constraints.NotBlank;

/**
 * Query para obtener plantillas de notificación.
 *
 * Estas anotaciones de validación son estándares de Java (JSR 303/380) y no introducen una dependencia
 * de un framework específico de infraestructura.
 *
 * Las validaciones se aplican en la capa de aplicación antes de que los datos lleguen al dominio,
 * actuando como un filtro inicial que protege la integridad del modelo de dominio.
 */
public record GetTemplatesQuery(
    
    @NotBlank
    String templateId,
    
    @NotBlank
    String templateType
) {}
