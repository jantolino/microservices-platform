package edu.market.userservice.infrastructure.adapter.input.web.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO para respuestas de autenticación
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Respuesta de autenticación exitosa con tokens y datos del usuario")
public class AuthResponseDTO {
    
    @Schema(description = "Token de acceso para autenticar solicitudes", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private String accessToken;
    
    @Schema(description = "Token de actualización para obtener un nuevo token de acceso", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private String refreshToken;
    
    @Schema(description = "Tipo de token, generalmente 'Bearer'", example = "Bearer")
    private String tokenType;
    
    @Schema(description = "Fecha y hora de expiración del token de acceso", example = "2023-01-01T12:00:00")
    private LocalDateTime expiresAt;
    
    @Schema(description = "Fecha y hora de expiración del token de actualización", example = "2023-01-08T12:00:00")
    private LocalDateTime refreshExpiresAt;
}
