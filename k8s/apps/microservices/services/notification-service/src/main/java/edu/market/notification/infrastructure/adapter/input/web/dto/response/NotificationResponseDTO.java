package edu.market.notification.infrastructure.adapter.input.web.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import edu.market.notification.domain.enums.EventStatusType;
import edu.market.notification.domain.enums.NotificationChannelType;
import edu.market.notification.domain.enums.NotificationPriorityType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

/**
 * DTO para respuestas de notificaciones a través de la API REST.
 * Este DTO está documentado con anotaciones de Swagger para la generación de documentación de API.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Notification details")
public record NotificationResponseDTO(
    /**
     * Identificador único de la notificación.
     */
    @Schema(description = "Unique identifier of the notification", 
            example = "123e4567-e89b-12d3-a456-426614174000")
    UUID id,
    
    /**
     * Asunto de la notificación.
     */
    @Schema(description = "Subject of the notification", 
            example = "Order confirmation")
    String subject,
    
    /**
     * Contenido de la notificación.
     */
    @Schema(description = "Content of the notification", 
            example = "Your order #12345 has been confirmed")
    String content,
    
    /**
     * Identificador del destinatario.
     */
    @Schema(description = "Recipient identifier", 
            example = "user123")
    String recipientId,
    
    /**
     * Correo electrónico del destinatario.
     */
    @Schema(description = "Recipient email address", 
            example = "user@example.com")
    String recipientEmail,
    
    /**
     * Canales por los que se envió la notificación.
     */
    @Schema(description = "Channels through which the notification was sent", 
            example = "[\"EMAIL\", \"SMS\"]")
    Set<NotificationChannelType> channels,
    
    /**
     * Prioridad de la notificación.
     */
    @Schema(description = "Priority level of the notification", 
            example = "HIGH")
    NotificationPriorityType priority,
    
    /**
     * Estado actual de la notificación.
     */
    @Schema(description = "Current status of the notification", 
            example = "SENT")
    EventStatusType status,
    
    /**
     * Fecha y hora de creación de la notificación.
     */
    @Schema(description = "Date and time when the notification was created", 
            example = "2025-07-09T19:46:19")
    LocalDateTime createdAt,
    
    /**
     * Fecha y hora programada para el envío de la notificación.
     */
    @Schema(description = "Date and time when the notification is scheduled to be sent", 
            example = "2025-07-09T20:00:00")
    LocalDateTime scheduledFor,
    
    /**
     * Fecha y hora de envío de la notificación.
     */
    @Schema(description = "Date and time when the notification was sent", 
            example = "2025-07-09T20:00:05")
    LocalDateTime sentAt,
    
    /**
     * Número de reintentos realizados.
     */
    @Schema(description = "Number of retry attempts", 
            example = "2")
    int retryCount,
    
    /**
     * Mensaje de error en caso de fallo.
     */
    @Schema(description = "Error message in case of failure", 
            example = "Recipient email address not found")
    String errorMessage,
    
    /**
     * Servicio de origen de la notificación.
     */
    @Schema(description = "Source service that triggered the notification", 
            example = "ORDER_SERVICE")
    String sourceService
) {}
