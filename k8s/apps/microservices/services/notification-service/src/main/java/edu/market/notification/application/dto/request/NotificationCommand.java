package edu.market.notification.application.dto.request;

import java.time.LocalDateTime;
import java.util.Set;

import edu.market.notification.domain.enums.NotificationChannelType;
import edu.market.notification.domain.enums.NotificationPriorityType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.FutureOrPresent;

/**
 * Command para representar una solicitud de notificación en los casos de uso.
 *
 * Estas anotaciones de validación son estándares de Java (JSR 303/380) y no introducen una dependencia
 * de un framework específico de infraestructura.
 *
 * Las validaciones se aplican en la capa de aplicación antes de que los datos lleguen al dominio,
 * actuando como un filtro inicial que protege la integridad del modelo de dominio.
 */
public record NotificationCommand(
    
    @NotBlank(message = "Subject cannot be blank")
    String subject,
    
    @NotBlank(message = "Content cannot be blank")
    String content,
    
    @NotBlank(message = "Recipient ID cannot be blank")
    String recipientId,
    
    String recipientEmail,
    
    @NotEmpty(message = "At least one notification channel must be specified")
    Set<NotificationChannelType> channels,
    
    @NotNull(message = "Priority cannot be null")
    NotificationPriorityType priority,
    
    @NotNull(message = "Scheduled date cannot be null")
    @FutureOrPresent(message = "Scheduled date must be in the present or future")
    LocalDateTime scheduledFor,
    
    String sourceService
) {
}
