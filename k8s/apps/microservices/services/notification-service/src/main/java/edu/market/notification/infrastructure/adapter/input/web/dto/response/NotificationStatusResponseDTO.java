package edu.market.notification.infrastructure.adapter.input.web.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import edu.market.notification.domain.enums.EventStatusType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO para respuestas de estado de notificaciones a través de la API REST.
 * Este DTO está documentado con anotaciones de Swagger para la generación de documentación de API.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Notification status details")
public record NotificationStatusResponseDTO(
    /**
     * Identificador único de la notificación.
     */
    @Schema(description = "Unique identifier of the notification", 
            example = "123e4567-e89b-12d3-a456-426614174000")
    UUID notificationId,
    
    /**
     * Estado actual de la notificación.
     */
    @Schema(description = "Current status of the notification", 
            example = "SENT")
    EventStatusType status,
    
    /**
     * Fecha y hora de la última actualización del estado.
     */
    @Schema(description = "Date and time of the last status update", 
            example = "2025-07-09T19:46:19")
    LocalDateTime lastUpdated,
    
    /**
     * Mensaje de error en caso de fallo.
     */
    @Schema(description = "Error message in case of failure", 
            example = "Recipient email address not found")
    String errorMessage,
    
    /**
     * Número de reintentos realizados.
     */
    @Schema(description = "Number of retry attempts", 
            example = "2")
    int retryCount,
    
    /**
     * Indica si la notificación puede ser reintentada.
     */
    @Schema(description = "Whether the notification can be retried", 
            example = "true")
    boolean canRetry
) {}
