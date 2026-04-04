package edu.market.userservice.application.port.input;

import edu.market.userservice.domain.model.UserAuthToken;
import edu.market.userservice.domain.vo.TokenRefreshRequestVO;

import java.util.Optional;

/**
 * Puerto de entrada para el caso de uso de renovación de token
 */
public interface RefreshTokenUseCasePort {
    
    /**
     * Renueva un token de autenticación utilizando un refresh token
     * 
     * @param refreshRequest Value Object con los datos de la solicitud de renovación
     * @return Nuevo token de autenticación o vacío si el refresh token es inválido o ha expirado
     */
    Optional<UserAuthToken> execute(TokenRefreshRequestVO refreshRequest);
}
