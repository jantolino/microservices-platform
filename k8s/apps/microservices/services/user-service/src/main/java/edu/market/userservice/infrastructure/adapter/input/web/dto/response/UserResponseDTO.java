package edu.market.userservice.infrastructure.adapter.input.web.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO para representar datos de un usuario en respuestas
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Datos de usuario para respuestas")
public class UserResponseDTO {
    
    @Schema(description = "Identificador único del usuario", example = "1")
    private Long id;
    
    @Schema(description = "Nombre completo del usuario", example = "John Doe")
    private String name;
    
    @Schema(description = "Nombre del usuario", example = "John")
    private String firstName;
    
    @Schema(description = "Apellido del usuario", example = "Doe")
    private String lastName;
    
    @Schema(description = "Correo electrónico del usuario", example = "john.doe@example.com")
    private String email;
    
    @Schema(description = "Número de teléfono del usuario", example = "+1234567890")
    private String phone;
    
    @Schema(description = "URL de la imagen de perfil del usuario", example = "https://example.com/profile.jpg")
    private String pictureUrl;
    
    @Schema(description = "Roles asignados al usuario", example = "[\"ROLE_USER\", \"ROLE_ADMIN\"]")
    private List<String> roles;
}
