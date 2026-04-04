package edu.market.notification.infrastructure.adapter.input.web.dto.request;

import edu.market.notification.domain.enums.ReportFormatType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO para recibir solicitudes de generación de informes de notificaciones a través de la API REST.
 * Este DTO está documentado con anotaciones de Swagger para la generación de documentación de API.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to generate a notification report")
public class GenerateReportRequestDTO {
    
    @NotNull(message = "userId required")
    @Schema(description = "User ID requesting the report", 
            example = "123e4567-e89b-12d3-a456-426614174000", 
            required = true)
    private UUID userId;
    
    @NotNull(message = "startDate required")
    @Schema(description = "Start date for the report period", 
            example = "2025-01-01T00:00:00", 
            required = true)
    private LocalDateTime startDate;
    
    @NotNull(message = "endDate required")
    @Schema(description = "End date for the report period", 
            example = "2025-07-09T23:59:59", 
            required = true)
    private LocalDateTime endDate;
    
    @NotNull(message = "reportFormat required")
    @Schema(description = "Format of the generated report", 
            example = "PDF", 
            required = true)
    private ReportFormatType reportFormat;
}
