package edu.market.notification.application.dto.request;

import edu.market.notification.domain.enums.ReportFormatType;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.validation.constraints.NotNull;

/**
 * Command para generar informes de notificaciones.
 * DTO que encapsula los parámetros necesarios
 * para generar un informe de notificaciones.
 *
 * Estas anotaciones de validación son estándares de Java (JSR 303/380) y no introducen una dependencia
 * de un framework específico de infraestructura.
 *
 * Las validaciones se aplican en la capa de aplicación antes de que los datos lleguen al dominio,
 * actuando como un filtro inicial que protege la integridad del modelo de dominio.
 */
public record GenerateReportCommand(

    @NotNull(message = "userId required")
    UUID userId,
    
    @NotNull(message = "startDate required")
    LocalDateTime startDate,
    
    @NotNull(message = "endDate required")
    LocalDateTime endDate,
    
    @NotNull(message = "reportFormat required")
    ReportFormatType reportFormat
) {}
