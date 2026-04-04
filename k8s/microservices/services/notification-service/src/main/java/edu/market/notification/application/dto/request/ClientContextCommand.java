package edu.market.notification.application.dto.request;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

/**
 * Command para recolectar información de contexto del cliente.
 * Este DTO encapsula los datos básicos relacionados con el contexto del cliente
 * que pueden ser útiles para auditoría y seguridad, limitándose a información
 * que puede ser capturada desde el servidor.
 *
 * Estas anotaciones de validación son estándares de Java (JSR 303/380) y no introducen una dependencia
 * de un framework específico de infraestructura.
 *
 * Las validaciones se aplican en la capa de aplicación antes de que los datos lleguen al dominio,
 * actuando como un filtro inicial que protege la integridad del modelo de dominio.
 */
public record ClientContextCommand(
    
    @NotNull(message = "requesterId required")
    UUID requesterId,
    
    String ipAddress,
    
    String userAgent,
    
    String deviceInfo
) {}
