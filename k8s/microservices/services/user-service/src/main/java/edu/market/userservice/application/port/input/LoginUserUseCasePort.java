package edu.market.userservice.application.port.input;

import edu.market.userservice.domain.model.UserAuthToken;
import edu.market.userservice.domain.vo.LoginCredentialsVO;
import edu.market.userservice.domain.vo.SocialLoginRequestVO;

/**
 * Puerto de entrada para el caso de uso de inicio de sesión
 */
public interface LoginUserUseCasePort {
    
    /**
     * Autentica a un usuario con sus credenciales
     * 
     * @param loginCredentialsVO Credenciales de inicio de sesión
     * @return Token de autenticación generado
     */
    UserAuthToken execute(LoginCredentialsVO loginCredentialsVO);

    /**
     * Autentica a un usuario mediante un proveedor de autenticación social
     * 
     * @param socialLoginRequestVO Value Object con los datos de la solicitud de autenticación social
     * @return Token de autenticación generado
     */
    UserAuthToken execute(SocialLoginRequestVO socialLoginRequestVO);
}
