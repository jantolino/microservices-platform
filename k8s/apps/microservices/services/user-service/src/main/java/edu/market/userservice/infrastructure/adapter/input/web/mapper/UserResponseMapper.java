package edu.market.userservice.infrastructure.adapter.input.web.mapper;

import edu.market.userservice.domain.model.User;
import edu.market.userservice.infrastructure.adapter.input.web.dto.response.UserResponseDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Mapper para convertir entidades de dominio a DTOs de respuesta
 */
@Slf4j
@Component
public class UserResponseMapper {

    /**
     * Convierte un User a UserResponseDTO
     *
     * @param user Objeto de dominio User
     * @return DTO UserResponseDTO para respuestas API
     */
    public UserResponseDTO toResponseDTO(User user) {
        try {
            log.info("init - toResponseDTO");
            
            if (user == null) {
                log.debug("toResponseDTO user is null, returning null");
                log.info("end - toResponseDTO");
                return null;
            }
            
            log.debug("toResponseDTO processing user with ID: {}", user.getId());
            
            UserResponseDTO dto = new UserResponseDTO();
            dto.setId(user.getId());
            dto.setName(user.getName());
            dto.setFirstName(user.getFirstName());
            dto.setLastName(user.getLastName());
            dto.setEmail(user.getEmail());
            dto.setPhone(user.getPhone());            
            dto.setRoles(
                user.getRoles() != null
                    ? user.getRoles().stream().map(role -> role.getName()).toList()
                    : null);
            
            log.debug("toResponseDTO DTO created successfully for user ID: {}", user.getId());
            log.info("end - toResponseDTO");
            return dto;
        } catch (Exception e) {
            log.error("toResponseDTO error: {}", e.getMessage(), e);
            throw e;
        }
    }
}
