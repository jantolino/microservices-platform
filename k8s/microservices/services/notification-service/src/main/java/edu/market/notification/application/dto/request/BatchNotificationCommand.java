package edu.market.notification.application.dto.request;

import java.util.List;
import java.util.UUID;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/**
 * Command para representar un comando de procesamiento por lotes de notificaciones.
 * Este DTO encapsula los datos necesarios para procesar múltiples notificaciones en una sola operación.
 *
 * Estas anotaciones de validación son estándares de Java (JSR 303/380) y no introducen una dependencia
 * de un framework específico de infraestructura. 
 * 
 * Las validaciones se aplican en la capa de aplicación antes de que los datos lleguen al dominio,
 * actuando como un filtro inicial que protege la integridad del modelo de dominio.
 */
public record BatchNotificationCommand(
    
    @NotNull(message = "batchId required")
    UUID batchId,
    
    @NotEmpty(message = "notifications required")
    @Size(max = 100, message = "batch size cannot exceed 100 notifications")
    @Valid
    List<NotificationCommand> notifications,
    
    @NotBlank(message = "source required")
    String source,
    
    @NotNull(message = "priority required")
    @Positive(message = "priority must be positive")
    Integer priority,
    
    boolean failFast
) {
    
}
