package edu.market.userservice.application.port.input;

import edu.market.userservice.domain.vo.LogoutAllRequestVO;
import edu.market.userservice.domain.vo.LogoutRequestVO;

/**
 * Puerto de entrada para el caso de uso de cierre de sesión
 */
public interface LogoutUserUseCasePort {
    
    /**
     * Cierra la sesión de un usuario
     * 
     * @param logoutRequest Value Object con la información de cierre de sesión
     * @return true si la sesión fue cerrada correctamente
     */
    boolean execute(LogoutRequestVO logoutRequest);

    /**
     * Cierra todas las sesiones de un usuario revocando todos sus tokens de acceso
     * 
     * @param logoutAllRequest Value Object con la información del usuario cuyas sesiones se cerrarán
     * @return Número de tokens revocados
     * @throws IllegalArgumentException si el usuario no existe
     */
    int execute(LogoutAllRequestVO logoutAllRequest);
}
