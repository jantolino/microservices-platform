package edu.market.notification.infrastructure.adapter.input.web.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * DTO para respuestas de consulta del historial de notificaciones a través de la API REST.
 * Este DTO está documentado con anotaciones de Swagger para la generación de documentación de API.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Notification history query result")
public record NotificationHistoryResultDTO(
    /**
     * Lista de notificaciones que coinciden con los criterios de búsqueda.
     */
    @Schema(description = "List of notifications matching the search criteria")
    List<NotificationResponseDTO> notifications,
    
    /**
     * Número total de notificaciones que coinciden con los criterios de búsqueda.
     */
    @Schema(description = "Total count of notifications matching the search criteria", 
            example = "150")
    int totalCount,
    
    /**
     * Límite de resultados por página.
     */
    @Schema(description = "Maximum number of results per page", 
            example = "20")
    int limit,
    
    /**
     * Desplazamiento para la paginación.
     */
    @Schema(description = "Offset for pagination", 
            example = "40")
    int offset,
    
    /**
     * Indica si hay más resultados disponibles.
     */
    @Schema(description = "Whether there are more results available", 
            example = "true")
    boolean hasMore
) {}
