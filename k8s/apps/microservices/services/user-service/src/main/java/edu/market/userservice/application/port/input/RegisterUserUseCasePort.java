package edu.market.userservice.application.port.input;

import edu.market.userservice.domain.model.UserAuthToken;
import edu.market.userservice.domain.vo.UserRegistrationVO;

/**
 * Puerto de entrada para el caso de uso de registro de usuario
 */
public interface RegisterUserUseCasePort {
    
    /**
     * Registra un nuevo usuario en el sistema
     * 
     * @param registration Datos de registro del usuario
     * @return Token de autenticación generado
     */
    UserAuthToken execute(UserRegistrationVO registration);
}
