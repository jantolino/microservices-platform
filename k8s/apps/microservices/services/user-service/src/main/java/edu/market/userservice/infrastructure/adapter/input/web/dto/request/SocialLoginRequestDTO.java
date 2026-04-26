package edu.market.userservice.infrastructure.adapter.input.web.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para solicitudes de inicio de sesión social
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Datos para autenticación mediante proveedor social (Google, Facebook, etc.)")
public class SocialLoginRequestDTO {
    
    @NotBlank(message = "El proveedor es obligatorio")
    @Schema(description = "Identificador del proveedor de autenticación", example = "google", required = true)
    private String provider;
    
    @NotBlank(message = "El token es obligatorio")
    @Schema(description = "Token de autenticación proporcionado por el proveedor social", required = true)
    private String token;
    
    @Email(message = "El formato del email no es válido")
    @Schema(description = "Correo electrónico del usuario en el proveedor social", example = "usuario@gmail.com")
    private String email;
    
    @Schema(description = "Nombre completo del usuario en el proveedor social", example = "John Doe")
    private String name;
    
    @Schema(description = "ID único del usuario en el proveedor social", example = "123456789")
    private String providerId;
    
    @Schema(description = "URL de la imagen de perfil del usuario", example = "https://lh3.googleusercontent.com/a/AATXAJxxxx")
    private String pictureUrl;
}
