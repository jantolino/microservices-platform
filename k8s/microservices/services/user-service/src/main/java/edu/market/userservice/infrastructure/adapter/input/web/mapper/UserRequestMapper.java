package edu.market.userservice.infrastructure.adapter.input.web.mapper;

import edu.market.userservice.domain.vo.PaginationRequestVO;
import edu.market.userservice.domain.vo.ProfileUpdateRequestVO;
import edu.market.userservice.infrastructure.adapter.input.web.dto.request.UserUpdateRequestDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Mapper para convertir DTOs de solicitud a Value Objects del dominio
 */
@Slf4j
@Component
public class UserRequestMapper {    

    /**
     * Convierte un DTO de actualización de usuario a un Value Object de actualización de perfil
     *
     * @param id ID del usuario a actualizar
     * @param requestDTO DTO con los datos de actualización
     * @return Value Object con los datos de actualización
     */
    public ProfileUpdateRequestVO toProfileUpdateRequestVO(Long id, UserUpdateRequestDTO requestDTO) {
        try {
            log.info("init - toProfileUpdateRequestVO");
            
            if (requestDTO == null) {
                log.debug("toProfileUpdateRequestVO requestDTO is null, returning null");
                log.info("end - toProfileUpdateRequestVO");
                return null;
            }
            
            log.debug("toProfileUpdateRequestVO processing update request for user ID: {}", id);
            
            ProfileUpdateRequestVO vo = new ProfileUpdateRequestVO(
                id,
                requestDTO.getFirstName(),
                requestDTO.getLastName(),
                requestDTO.getPhone()
            );
            
            log.debug("toProfileUpdateRequestVO VO created successfully for user ID: {}", id);
            log.info("end - toProfileUpdateRequestVO");
            return vo;
        } catch (Exception e) {
            log.error("toProfileUpdateRequestVO error: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Crea un Value Object de paginación con los parámetros especificados
     *
     * @param page Número de página
     * @param size Tamaño de página
     * @return Value Object con los parámetros de paginación
     */
    public PaginationRequestVO toPaginationRequestVO(int page, int size) {
        try {
            log.info("init - toPaginationRequestVO");
            
            log.debug("toPaginationRequestVO processing pagination request with page: {} and size: {}", page, size);
            
            PaginationRequestVO vo = new PaginationRequestVO(page, size);
            
            log.debug("toPaginationRequestVO VO created successfully");
            log.info("end - toPaginationRequestVO");
            return vo;
        } catch (Exception e) {
            log.error("toPaginationRequestVO error: {}", e.getMessage(), e);
            throw e;
        }
    }
}
