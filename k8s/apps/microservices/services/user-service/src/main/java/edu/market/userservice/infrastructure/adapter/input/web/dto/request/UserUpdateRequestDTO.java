package edu.market.userservice.infrastructure.adapter.input.web.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para solicitudes de actualización de datos de usuario
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Datos para actualizar un usuario existente")
public class UserUpdateRequestDTO {
    
    @Schema(description = "Nombre completo del usuario", example = "John Doe")
    private String name;
    
    @Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres")
    @Schema(description = "Nombre del usuario", example = "John")
    private String firstName;
    
    @Size(min = 2, max = 100, message = "El apellido debe tener entre 2 y 100 caracteres")
    @Schema(description = "Apellido del usuario", example = "Doe")
    private String lastName;
    
    @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "El formato del teléfono no es válido")
    @Schema(description = "Número de teléfono del usuario", example = "+1234567890")
    private String phone;
}
