package edu.market.notification.infrastructure.adapter.input.web.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO para recibir solicitudes de consulta del historial de notificaciones a través de la API REST.
 * Este DTO está documentado con anotaciones de Swagger para la generación de documentación de API.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to query notification history")
public class NotificationHistoryRequestDTO {
    
    @NotNull(message = "userId required")
    @Schema(description = "User ID to filter notifications", 
            example = "123e4567-e89b-12d3-a456-426614174000", 
            required = true)
    private UUID userId;
    
    @NotNull(message = "notificationType required")
    @Schema(description = "Type of notifications to filter", 
            example = "EMAIL", 
            required = true)
    private String notificationType;
    
    @NotNull(message = "status required")
    @Schema(description = "Status of notifications to filter", 
            example = "SENT", 
            required = true)
    private String status;
    
    @NotNull(message = "startDate required")
    @Schema(description = "Start date for the history period", 
            example = "2025-01-01T00:00:00", 
            required = true)
    private LocalDateTime startDate;
    
    @NotNull(message = "endDate required")
    @Schema(description = "End date for the history period", 
            example = "2025-07-09T23:59:59", 
            required = true)
    private LocalDateTime endDate;
    
    @Min(value = 1, message = "limit must be at least 1")
    @Schema(description = "Maximum number of results to return", 
            example = "20", 
            required = true)
    private int limit;
    
    @PositiveOrZero(message = "offset must be non-negative")
    @Schema(description = "Number of results to skip for pagination", 
            example = "0", 
            required = true)
    private int offset;
}
