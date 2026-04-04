package edu.market.notification.infrastructure.adapter.input.web.api;

import edu.market.notification.infrastructure.adapter.input.web.dto.request.DisableNotificationsRequestDTO;
import edu.market.notification.infrastructure.adapter.input.web.dto.request.UpdateUserPreferencesRequestDTO;
import edu.market.notification.infrastructure.adapter.input.web.dto.response.ErrorResponse;
import edu.market.notification.infrastructure.adapter.input.web.dto.response.UserPreferenceResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

/**
 * API para la gestión de preferencias de notificaciones de usuarios.
 * Esta interfaz define los endpoints relacionados con la obtención y actualización
 * de las preferencias de notificaciones de los usuarios.
 */
@Tag(name = "Usuarios", description = "API para la gestión de preferencias de notificaciones de usuarios")
@RequestMapping("/api")
public interface UserApi {

    /**
     * Obtiene las preferencias de notificaciones para un usuario específico.
     *
     * @param userId Identificador único del usuario
     * @return Respuesta con las preferencias de notificaciones del usuario
     */
    @Operation(
            summary = "Obtener preferencias de usuario",
            description = "Obtiene las preferencias de notificaciones configuradas para un usuario específico"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Preferencias obtenidas con éxito",
                    content = @Content(schema = @Schema(implementation = UserPreferenceResponseDTO.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Solicitud inválida",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Error interno del servidor",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @GetMapping("/users/{userId}/preferences")
    ResponseEntity<UserPreferenceResponseDTO> getUserPreferences(
            @Parameter(description = "ID de usuario", required = true)
            @PathVariable String userId);

    /**
     * Actualiza las preferencias de notificaciones para un usuario específico.
     *
     * @param userId Identificador único del usuario
     * @param requestDTO DTO con las preferencias de notificaciones actualizadas
     * @return Respuesta con las preferencias de notificaciones actualizadas del usuario
     */
    @Operation(
            summary = "Actualizar preferencias de usuario",
            description = "Actualiza las preferencias de notificaciones para un usuario específico"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Preferencias actualizadas con éxito",
                    content = @Content(schema = @Schema(implementation = UserPreferenceResponseDTO.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Datos de preferencia inválidos",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Error interno del servidor",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @PutMapping("/users/{userId}/preferences")
    ResponseEntity<UserPreferenceResponseDTO> updateUserPreferences(
            @Parameter(description = "ID de usuario", required = true)
            @PathVariable String userId,
            @Parameter(description = "Nuevas preferencias de notificación", required = true)
            @Valid @RequestBody UpdateUserPreferencesRequestDTO requestDTO);

    /**
     * Deshabilita todas las notificaciones para un usuario específico.
     *
     * @param userId Identificador único del usuario
     * @return Respuesta confirmando que las notificaciones han sido deshabilitadas
     */
    @Operation(
            summary = "Deshabilitar notificaciones",
            description = "Deshabilita todas las notificaciones para un usuario específico"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "Notifications disabled successfully",
                    content = @Content
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
    @PostMapping("/users/{userId}/disable-notifications")
    ResponseEntity<Void> disableNotifications(
        @Parameter(description = "User ID", required = true)
        @PathVariable String userId,
        @Parameter(description = "Disable notifications request", required = true)
        @Valid @RequestBody DisableNotificationsRequestDTO requestDTO);

    /**
     * Enables notifications for a user.
     *
     * @param userId Unique identifier of the user
     * @return Confirmation of the operation
     */
    //@Operation(
    //        summary = "Enable notifications",
    //        description = "Enables notifications for a user who had them disabled"
    //)
    //@ApiResponses(value = {
    //        @ApiResponse(
    //                responseCode = "204",
    //                description = "Notifications enabled successfully",
    //                content = @Content
    //        ),
    //        @ApiResponse(
    //                responseCode = "400",
    //                description = "Invalid request",
    //                content = @Content(schema = @Schema(implementation = ErrorResponse.class))
    //        ),
    //        @ApiResponse(
    //                responseCode = "500",
    //                description = "Internal server error",
    //                content = @Content(schema = @Schema(implementation = ErrorResponse.class))
    //        )
    //})
    //@PostMapping("/users/{userId}/enable-notifications")
    //ResponseEntity<Void> enableNotifications(
    //        @Parameter(description = "User ID", required = true)
    //        @PathVariable String userId);
}
