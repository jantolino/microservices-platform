package edu.market.notification.application.dto.request;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;

/**
 * Command para reintentar el envío de una notificación fallida.
 * Siguiendo el patrón CQRS, este comando representa una intención de modificación.
 *
 * Estas anotaciones de validación son estándares de Java (JSR 303/380) y no introducen una dependencia
 * de un framework específico de infraestructura.
 *
 * Las validaciones se aplican en la capa de aplicación antes de que los datos lleguen al dominio,
 * actuando como un filtro inicial que protege la integridad del modelo de dominio.
 */
public record RetryNotificationCommand(
    @NotNull(message = "notificationId required")
    UUID notificationId
) {}
