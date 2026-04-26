package edu.market.notification.application.dto.request;

import edu.market.notification.domain.enums.SourceServiceType;
import jakarta.validation.constraints.NotBlank;

/**
 * Command para suscribir el servicio a eventos específicos de otros microservicios.
 * Este command es inmutable y encapsula los parámetros necesarios para la operación.
 *
 * Estas anotaciones de validación son estándares de Java (JSR 303/380) y no introducen una dependencia
 * de un framework específico de infraestructura.
 *
 * Las validaciones se aplican en la capa de aplicación antes de que los datos lleguen al dominio,
 * actuando como un filtro inicial que protege la integridad del modelo de dominio.
 */
public record SubscribeToEventsCommand(
    
    @NotBlank(message = "eventType required")
    String eventType,
    
    @NotBlank(message = "sourceService required")
    SourceServiceType sourceService
) {}
