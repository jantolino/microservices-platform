package edu.market.userservice.domain.port.output;

import edu.market.userservice.domain.model.User;
import edu.market.userservice.domain.model.UserAuthToken;
import edu.market.userservice.domain.vo.TokenCreateVO;
import edu.market.userservice.domain.vo.TokenRefreshVO;

import java.util.Map;
import java.util.Optional;

/**
 * Puerto de entrada para el servicio de dominio de tokens
 */
public interface TokenDomainServicePort {
    
    /**
     * Crea un objeto UserAuthToken utilizando un Value Object
     * 
     * @param tokenCreateVO Value Object con los datos para crear el token
     * @return Objeto UserAuthToken
     */
    UserAuthToken createUserAuthToken(TokenCreateVO tokenCreateVO);
    
    /**
     * Prepara los claims para un token de acceso
     * 
     * @param user Usuario para el que se genera el token
     * @return Mapa de claims
     */
    Map<String, Object> prepareAccessTokenClaims(User user);
    
    /**
     * Prepara los claims para un token de actualización
     * 
     * @param user Usuario para el que se genera el token
     * @return Mapa de claims
     */
    Map<String, Object> prepareRefreshTokenClaims(User user);    
    
    
    /**
     * Genera un token de autenticación para un usuario
     * 
     * @param user Usuario para el que se genera el token
     * @return Token de autenticación generado
     */
    UserAuthToken generateToken(User user);
    
    /**
     * Refresca un token de autenticación existente utilizando un Value Object
     * 
     * @param refreshVO Value Object con los datos para refrescar el token
     * @return Token refrescado
     */
    UserAuthToken refreshToken(TokenRefreshVO refreshVO);
    
    /**
     * Revoca un token específico
     * 
     * @param token Token a revocar (puede ser access token o refresh token)
     * @return true si el token fue revocado correctamente
     */
    boolean revokeToken(String token);
    
    /**
     * Revoca todos los tokens de un usuario
     * 
     * @param userId ID del usuario cuyos tokens serán revocados
     * @return Número de tokens revocados
     */
    int revokeAllTokensForUser(Long userId);
    
    /**
     * Busca un token activo para un usuario y proveedor social
     * 
     * @param userId ID del usuario
     * @param provider Proveedor social (ej. "GOOGLE", "FACEBOOK")
     * @return Token activo si existe
     */
    Optional<UserAuthToken> findActiveSocialToken(Long userId, String provider);
}
