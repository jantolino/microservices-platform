package edu.market.notification.infrastructure.adapter.input.web.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

/**
 * DTO para recibir solicitudes de procesamiento por lotes de notificaciones a través de la API REST.
 * Este DTO está documentado con anotaciones de Swagger para la generación de documentación de API.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to process a batch of notifications")
public class BatchNotificationRequestDTO {
    
    @NotNull(message = "batchId required")
    @Schema(description = "Unique identifier for the batch", 
            example = "123e4567-e89b-12d3-a456-426614174000", 
            required = true)
    private UUID batchId;
    
    @NotEmpty(message = "notifications required")
    @Size(max = 100, message = "batch size cannot exceed 100 notifications")
    @Valid
    @Schema(description = "List of notifications to be processed in batch", 
            required = true)
    private List<NotificationRequestDTO> notifications;
    
    @NotBlank(message = "source required")
    @Schema(description = "Source system or application sending the batch", 
            example = "ORDER_SERVICE", 
            required = true)
    private String source;
    
    @NotNull(message = "priority required")
    @Positive(message = "priority must be positive")
    @Schema(description = "Priority level for the batch processing", 
            example = "1", 
            required = true)
    private Integer priority;
    
    @Schema(description = "Whether to stop processing on first error", 
            example = "false")
    private boolean failFast;
}
