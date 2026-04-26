package edu.market.userservice.infrastructure.adapter.input.web.mapper;

import org.springframework.stereotype.Component;
import edu.market.userservice.domain.model.User;
import edu.market.userservice.domain.model.UserAuthToken;
import edu.market.userservice.infrastructure.adapter.input.web.dto.response.AuthResponseDTO;
import edu.market.userservice.infrastructure.adapter.input.web.dto.response.UserResponseDTO;

import java.util.stream.Collectors;

/**
 * Mapper para convertir modelos de dominio a DTOs de respuesta
 */
@Component
public class AuthResponseMapper {
    
    /**
     * Convierte un token de autenticación a un DTO de respuesta de autenticación
     * 
     * @param token Token de autenticación
     * @return DTO de respuesta de autenticación
     */
    public AuthResponseDTO toAuthResponseDTO(UserAuthToken token) {
        AuthResponseDTO responseDTO = new AuthResponseDTO();
        responseDTO.setAccessToken(token.getAccessToken());
        responseDTO.setRefreshToken(token.getRefreshToken());
        responseDTO.setTokenType("Bearer");
        responseDTO.setExpiresAt(token.getExpiresAt());
        responseDTO.setRefreshExpiresAt(token.getRefreshExpiresAt());
        
        // Obtener el usuario desde el repositorio de usuarios usando token.getUserId()
        // Por ahora, no establecemos el usuario directamente ya que el token no tiene una referencia directa al objeto User
        // Esto se debe hacer en el servicio que llama a este mapper
        
        return responseDTO;
    }
    
    /**
     * Convierte un usuario a un DTO de respuesta de usuario
     * 
     * @param user Usuario
     * @return DTO de respuesta de usuario
     */
    public UserResponseDTO toUserResponseDTO(User user) {
        UserResponseDTO responseDTO = new UserResponseDTO();
        responseDTO.setId(user.getId());
        responseDTO.setEmail(user.getEmail());
        responseDTO.setFirstName(user.getFirstName());
        responseDTO.setLastName(user.getLastName());
        
        // Establecer nombre completo
        String fullName = user.getFirstName() + " " + user.getLastName();
        responseDTO.setName(fullName.trim());
        
        // Establecer teléfono si existe
        if (user.getPhone() != null) {
            responseDTO.setPhone(user.getPhone());
        }
                
        // Establecer roles si existen
        if (user.getRoles() != null) {
            responseDTO.setRoles(user.getRoles().stream()
                    .map(role -> role.getName())
                    .collect(Collectors.toList()));
        }
        
        return responseDTO;
    }
}
