package edu.market.notification.application.dto.request;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

/**
 * DTO que encapsula los parámetros de consulta para obtener el historial de notificaciones.
 *
 * Estas anotaciones de validación son estándares de Java (JSR 303/380) y no introducen una dependencia
 * de un framework específico de infraestructura. 
 *
 * Las validaciones se aplican en la capa de aplicación antes de que los datos lleguen al dominio,
 * actuando como un filtro inicial que protege la integridad del modelo de dominio.
 */
public record NotificationHistoryQuery(
    
    @NotNull(message = "userId required")
    UUID userId,
    
    @NotNull(message = "notificationType required")
    String notificationType,
    
    @NotNull(message = "status required")
    String status,
    
    @NotNull(message = "startDate required")
    LocalDateTime startDate,
    
    @NotNull(message = "endDate required")
    LocalDateTime endDate,
    
    @Min(value = 1, message = "limit must be at least 1")
    int limit,
    
    @PositiveOrZero(message = "offset must be non-negative")
    int offset
) {}
