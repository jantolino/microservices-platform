package edu.market.notification.infrastructure.adapter.input.web.api;

import edu.market.notification.infrastructure.adapter.input.web.dto.request.ProcessEventRequestDTO;
import edu.market.notification.infrastructure.adapter.input.web.dto.request.SubscribeToEventsRequestDTO;
import edu.market.notification.infrastructure.adapter.input.web.dto.request.UnsubscribeFromEventsRequestDTO;
import edu.market.notification.infrastructure.adapter.input.web.dto.response.ErrorResponse;
import edu.market.notification.infrastructure.adapter.input.web.dto.response.NotificationResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import jakarta.validation.Valid;

/**
 * API para procesamiento de eventos de dominio.
 * Esta interfaz define los endpoints relacionados con la recepción y procesamiento
 * de eventos de dominio que pueden desencadenar notificaciones, así como la gestión
 * de suscripciones a eventos.
 */
@Tag(name = "Events", description = "API for processing and subscribing to domain events")
@RequestMapping("/api")
public interface EventApi {

    /**
     * Procesa un evento de dominio y genera las notificaciones correspondientes según las reglas configuradas.
     *
     * @param requestDTO DTO con información del evento de dominio
     * @return Respuesta con el resultado del procesamiento del evento
     */
    @Operation(
            summary = "Process domain event",
            description = "Receives a domain event and generates notifications according to configured rules"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "202",
                    description = "Event accepted for processing",
                    content = @Content(schema = @Schema(implementation = NotificationResponseDTO.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid event data",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @PostMapping("/events")
    ResponseEntity<NotificationResponseDTO> processEvent(
            @Parameter(description = "Domain event to process", required = true)
            @Valid @RequestBody ProcessEventRequestDTO requestDTO);
    
    /**
     * Suscribe el servicio a un tipo de evento específico de otro microservicio.
     *
     * @param requestDTO DTO con información de suscripción a eventos
     * @return Confirmación de la suscripción
     */
    @Operation(
            summary = "Subscribe to events",
            description = "Subscribes the service to receive events of a specific type from another microservice"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Successfully subscribed",
                    content = @Content(schema = @Schema(implementation = Boolean.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid subscription data",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @PutMapping("/events/subscriptions")
    ResponseEntity<Boolean> subscribeToEvents(
            @Parameter(description = "Event subscription information", required = true)
            @Valid @RequestBody SubscribeToEventsRequestDTO requestDTO);
    
    /**
     * Cancela una suscripción a un tipo de evento específico de otro microservicio.
     *
     * @param requestDTO DTO con información de cancelación de suscripción
     * @return Confirmación de la cancelación de la suscripción
     */
    @Operation(
            summary = "Unsubscribe from events",
            description = "Cancels subscription to events of a specific type from another microservice"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Successfully unsubscribed",
                    content = @Content(schema = @Schema(implementation = Boolean.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid unsubscription data",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @DeleteMapping("/events/subscriptions")
    ResponseEntity<Boolean> unsubscribeFromEvents(
            @Parameter(description = "Subscription cancellation information", required = true)
            @Valid @RequestBody UnsubscribeFromEventsRequestDTO requestDTO);
}
