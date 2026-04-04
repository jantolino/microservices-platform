package edu.market.userservice.application.port.input;

import edu.market.userservice.domain.model.User;
import edu.market.userservice.domain.vo.PasswordChangeVO;

public interface ResetPasswordUseCasePort {
    /**
     * Restablece la contraseña de un usuario
     *
     * @param passwordChangeData Datos para el restablecimiento de contraseña
     * @return Usuario con la contraseña actualizada
     */
    User execute(PasswordChangeVO passwordChangeData);
}
