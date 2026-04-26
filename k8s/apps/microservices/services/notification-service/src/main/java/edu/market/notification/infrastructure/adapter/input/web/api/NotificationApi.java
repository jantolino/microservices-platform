package edu.market.notification.infrastructure.adapter.input.web.api;

import edu.market.notification.infrastructure.adapter.input.web.dto.request.GenerateReportRequestDTO;
import edu.market.notification.infrastructure.adapter.input.web.dto.request.NotificationHistoryRequestDTO;
import edu.market.notification.infrastructure.adapter.input.web.dto.request.NotificationRequestDTO;
import edu.market.notification.infrastructure.adapter.input.web.dto.response.ErrorResponse;
import edu.market.notification.infrastructure.adapter.input.web.dto.response.NotificationHistoryResultDTO;
import edu.market.notification.infrastructure.adapter.input.web.dto.response.NotificationResponseDTO;
import edu.market.notification.infrastructure.adapter.input.web.dto.response.NotificationStatusResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

/**
 * API para la gestión de notificaciones.
 * Esta interfaz define los endpoints relacionados con la programación, consulta y
 * cancelación de notificaciones.
 */
@Tag(name = "Notifications", description = "API for notification management")
@RequestMapping("/api")
public interface NotificationApi {

    /**
     * Programa una nueva notificación para ser enviada.
     *
     * @param requestDTO DTO con la información de la notificación a programar
     * @return Respuesta con los detalles de la notificación programada
     */
    @Operation(
            summary = "Schedule notification",
            description = "Schedules a new notification to be sent on the specified date"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Notification scheduled successfully",
                    content = @Content(schema = @Schema(implementation = NotificationResponseDTO.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid notification data",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @PostMapping("/notifications")
    ResponseEntity<NotificationResponseDTO> scheduleNotification(
            @Parameter(description = "Notification information to schedule", required = true)
            @Valid @RequestBody NotificationRequestDTO requestDTO);

    /**
     * Cancela una notificación programada.
     *
     * @param id Identificador único de la notificación a cancelar
     * @return Confirmación de la cancelación
     */
    @Operation(
            summary = "Cancel notification",
            description = "Cancels a scheduled notification that has not been sent yet"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Notification cancelled successfully",
                    content = @Content(schema = @Schema(implementation = Boolean.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Notification cannot be cancelled",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @DeleteMapping("/notifications/{id}")
    ResponseEntity<NotificationResponseDTO> cancelNotification(
            @Parameter(description = "ID of the notification to cancel", required = true)
            @PathVariable String id);

    /**
     * Verifica el estado actual de una notificación.
     *
     * @param id Identificador único de la notificación
     * @return Estado actual de la notificación
     */
    @Operation(
            summary = "Check notification status",
            description = "Gets the current status of a specific notification"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Status retrieved successfully",
                    content = @Content(schema = @Schema(implementation = NotificationStatusResponseDTO.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @GetMapping("/notifications/{id}/status")
    ResponseEntity<NotificationStatusResponseDTO> checkNotificationStatus(
            @Parameter(description = "Notification ID", required = true)
            @PathVariable String id);

    /**
     * Reintenta el envío de una notificación que falló previamente.
     *
     * @param id Identificador único de la notificación
     * @return Respuesta con los detalles de la notificación reintentada
     */
    @Operation(
            summary = "Retry notification",
            description = "Retries sending a notification that previously failed"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Retry initiated successfully",
                    content = @Content(schema = @Schema(implementation = NotificationResponseDTO.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Notification cannot be retried",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @PostMapping("/notifications/{id}/retry")
    ResponseEntity<NotificationResponseDTO> retryNotification(
            @Parameter(description = "ID of the notification to retry", required = true)
            @PathVariable String id);

    /**
     * Envía una notificación inmediatamente sin programación.
     *
     * @param requestDTO DTO con la información de la notificación a enviar
     * @return Respuesta con los detalles de la notificación enviada
     */
    @Operation(
            summary = "Send immediate notification",
            description = "Sends a notification immediately without prior scheduling"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Notification sent successfully",
                    content = @Content(schema = @Schema(implementation = NotificationResponseDTO.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid notification data",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @PostMapping("/notifications/send")
    ResponseEntity<NotificationResponseDTO> sendNotification(
            @Parameter(description = "Notification information to send", required = true)
            @Valid @RequestBody NotificationRequestDTO requestDTO);
            
    /**
     * Genera un informe de notificaciones basado en filtros.
     *
     * @param requestDTO DTO con la información para generar el informe de notificaciones
     * @return Recurso descargable con el informe generado
     */
    @Operation(
            summary = "Generate notification report",
            description = "Generates a notification report based on optional filters"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Report generated successfully",
                    content = @Content(schema = @Schema(implementation = Resource.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid filter parameters",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @GetMapping("/notifications/report")
    ResponseEntity<byte[]> generateNotificationReport(
            @Parameter(description = "Report request")
            @Valid @RequestBody GenerateReportRequestDTO requestDTO);
            
    /**
     * Obtiene el historial de notificaciones de un usuario.
     *
     * @param requestDTO DTO con la información para solicitar el historial de notificaciones
     * @return Lista de notificaciones históricas del usuario
     */
    @Operation(
            summary = "Get notification history",
            description = "Retrieves the notification history for a specific user"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "History retrieved successfully",
                    content = @Content(schema = @Schema(implementation = NotificationHistoryResultDTO.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @GetMapping("/notifications/history")
    ResponseEntity<NotificationHistoryResultDTO> getNotificationHistory(
            @Parameter(description = "Notification history request", required = true)
            @Valid @RequestBody NotificationHistoryRequestDTO requestDTO);
}
