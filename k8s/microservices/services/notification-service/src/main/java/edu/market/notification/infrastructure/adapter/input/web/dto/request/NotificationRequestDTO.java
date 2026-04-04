package edu.market.notification.infrastructure.adapter.input.web.dto.request;

import java.time.LocalDateTime;
import java.util.Set;

import edu.market.notification.domain.enums.NotificationChannelType;
import edu.market.notification.domain.enums.NotificationPriorityType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para recibir solicitudes de programación o envío de notificaciones a través de la API REST.
 * Este DTO está documentado con anotaciones de Swagger para la generación de documentación de API.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to schedule or send a notification")
public class NotificationRequestDTO {
    
    @NotBlank(message = "subject required")
    @Schema(description = "Subject of the notification", 
            example = "Registration Confirmation", 
            required = true)
    private String subject;
    
    @NotBlank(message = "content required")
    @Schema(description = "Content of the notification", 
            example = "Your account has been successfully registered", 
            required = true)
    private String content;
    
    @NotBlank(message = "recipientId required")
    @Schema(description = "Unique identifier of the recipient", 
            example = "user-123", 
            required = true)
    private String recipientId;
    
    @Schema(description = "Email address of the recipient", 
            example = "user@example.com")
    private String recipientEmail;
    
    @NotEmpty(message = "channels required")
    @Schema(description = "Channels through which the notification will be sent", 
            required = true,
            example = "[\"EMAIL\", \"SMS\"]")
    private Set<NotificationChannelType> channels;
    
    @NotNull(message = "priority required")
    @Schema(description = "Priority of the notification", 
            required = true,
            example = "HIGH")
    private NotificationPriorityType priority;
    
    @NotNull(message = "scheduledFor required")
    @FutureOrPresent(message = "scheduledFor must be in the present or future")
    @Schema(description = "Date and time scheduled for sending", 
            required = true,
            example = "2025-07-10T10:30:00")
    private LocalDateTime scheduledFor;
    
    @Schema(description = "Service that originates the notification", 
            example = "USER_SERVICE")
    private String sourceService;
}
