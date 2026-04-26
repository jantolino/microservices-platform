package edu.market.userservice.application.port.input;

import edu.market.userservice.domain.model.User;
import edu.market.userservice.domain.vo.ProfileUpdateRequestVO;

/**
 * Puerto de entrada para el caso de uso de actualización de perfil de usuario
 */
public interface UpdateUserProfileUseCasePort {
    
    /**
     * Actualiza el perfil de un usuario
     * 
     * @param profileUpdateRequest Value Object con los datos de actualización del perfil
     * @return Usuario actualizado
     */
    User execute(ProfileUpdateRequestVO profileUpdateRequest);
}
