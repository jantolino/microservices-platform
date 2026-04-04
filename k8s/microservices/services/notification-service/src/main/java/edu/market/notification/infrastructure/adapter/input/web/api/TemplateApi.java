package edu.market.notification.infrastructure.adapter.input.web.api;

import edu.market.notification.infrastructure.adapter.input.web.dto.request.CreateTemplateRequestDTO;
import edu.market.notification.infrastructure.adapter.input.web.dto.request.GetTemplatesRequestDTO;
import edu.market.notification.infrastructure.adapter.input.web.dto.request.UpdateTemplateRequestDTO;
import edu.market.notification.infrastructure.adapter.input.web.dto.response.ErrorResponse;
import edu.market.notification.infrastructure.adapter.input.web.dto.response.TemplateResponseDTO;
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
import java.util.List;

/**
 * API para la gestión de plantillas de notificaciones.
 * Esta interfaz define los endpoints relacionados con la creación, consulta y
 * actualización de plantillas de notificaciones.
 */
@Tag(name = "Templates", description = "API for notification template management")
@RequestMapping("/api")
public interface TemplateApi {

    /**
     * Crea una nueva plantilla de notificación.
     *
     * @param requestDTO DTO con la información de la plantilla a crear
     * @return Respuesta con los detalles de la plantilla creada
     */
    @Operation(
            summary = "Create template",
            description = "Creates a new notification template for later use"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Template created successfully",
                    content = @Content(schema = @Schema(implementation = TemplateResponseDTO.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid template data",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @PostMapping("/templates")
    ResponseEntity<TemplateResponseDTO> createTemplate(
            @Parameter(description = "Template to create", required = true)
            @Valid @RequestBody CreateTemplateRequestDTO requestDTO);

    /**
     * Obtiene los detalles de una plantilla específica.
     *
     * @param code Código único de la plantilla
     * @return Respuesta con los detalles de la plantilla
     */
    @Operation(
            summary = "Get template",
            description = "Retrieves the details of a specific template by its code"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Template found",
                    content = @Content(schema = @Schema(implementation = TemplateResponseDTO.class))
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
    @GetMapping("/templates")
    ResponseEntity<List<TemplateResponseDTO>> getTemplates(
            @Parameter(description = "Template code", required = true)
            @Valid @RequestBody GetTemplatesRequestDTO requestDTO);

    /**
     * Actualiza una plantilla existente.
     *
     * @param templateId Identificador único de la plantilla a actualizar
     * @param requestDTO DTO con la información actualizada de la plantilla
     * @return Respuesta con los detalles de la plantilla actualizada
     */
    @Operation(
            summary = "Update template",
            description = "Updates an existing notification template"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Template updated successfully",
                    content = @Content(schema = @Schema(implementation = TemplateResponseDTO.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid template data",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @PutMapping("/templates/{templateId}")
    ResponseEntity<TemplateResponseDTO> updateTemplate(
            @Parameter(description = "ID of the template to update", required = true)
            @PathVariable String templateId,
            @Parameter(description = "Updated template data", required = true)
            @Valid @RequestBody UpdateTemplateRequestDTO requestDTO);
            
    /**
     * Elimina una plantilla existente.
     *
     * @param templateId Identificador único de la plantilla a eliminar
     * @return Confirmación de la eliminación
     */
    @Operation(
            summary = "Delete template",
            description = "Deletes an existing notification template"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "Template deleted successfully",
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
    @DeleteMapping("/templates/{templateId}")
    ResponseEntity<Void> deleteTemplate(
            @Parameter(description = "ID of the template to delete", required = true)
            @PathVariable String templateId);
}
