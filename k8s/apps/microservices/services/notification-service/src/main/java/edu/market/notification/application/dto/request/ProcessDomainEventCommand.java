package edu.market.notification.application.dto.request;

import java.util.Map;

import jakarta.validation.constraints.NotNull;

/**
 * Comando para procesar un evento de dominio y generar notificaciones.
 * Este comando encapsula los datos necesarios para procesar un evento.
 *
 * Estas anotaciones de validación son estándares de Java (JSR 303/380) y no introducen una dependencia
 * de un framework específico de infraestructura. 
 * 
 * Las validaciones se aplican en la capa de aplicación antes de que los datos lleguen al dominio,
 * actuando como un filtro inicial que protege la integridad del modelo de dominio.
 */
public record ProcessDomainEventCommand(
    String eventId,
    
    @NotNull(message = "eventType required")
    String eventType,
    
    @NotNull(message = "source required")
    String source,
    
    @NotNull(message = "payload required")
    Map<String, Object> payload,
    
    String correlationId
) {}
