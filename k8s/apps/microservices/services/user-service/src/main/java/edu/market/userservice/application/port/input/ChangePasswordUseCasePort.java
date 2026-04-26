package edu.market.userservice.application.port.input;

import edu.market.userservice.domain.model.User;
import edu.market.userservice.domain.vo.PasswordChangeVO;

/**
 * Puerto de entrada para el caso de uso de cambio de contraseña
 */
public interface ChangePasswordUseCasePort {
    
    /**
     * Cambia la contraseña de un usuario
     * 
     * @param passwordChangeData Datos para el cambio de contraseña
     * @return Usuario con la contraseña actualizada
     */
    User execute(PasswordChangeVO passwordChangeData);
}
