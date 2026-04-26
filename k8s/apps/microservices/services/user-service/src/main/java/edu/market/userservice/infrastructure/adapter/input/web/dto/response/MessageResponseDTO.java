package edu.market.userservice.infrastructure.adapter.input.web.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para respuestas de mensajes genéricos
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Respuesta genérica con mensaje informativo")
public class MessageResponseDTO {
    
    @Schema(description = "Mensaje informativo", example = "Operación completada con éxito")
    private String message;
    
    @Schema(description = "Indica si la operación fue exitosa", example = "true")
    private boolean success;
    
    @Schema(description = "Marca de tiempo en milisegundos desde epoch", example = "1625097600000")
    private Long timestamp;
    
    /**
     * Crea una respuesta exitosa
     * @param message Mensaje de éxito
     * @return DTO de respuesta
     */
    public static MessageResponseDTO success(String message) {
        return MessageResponseDTO.builder()
                .message(message)
                .success(true)
                .timestamp(System.currentTimeMillis())
                .build();
    }
    
    /**
     * Crea una respuesta de error
     * @param message Mensaje de error
     * @return DTO de respuesta
     */
    public static MessageResponseDTO error(String message) {
        return MessageResponseDTO.builder()
                .message(message)
                .success(false)
                .timestamp(System.currentTimeMillis())
                .build();
    }
}
