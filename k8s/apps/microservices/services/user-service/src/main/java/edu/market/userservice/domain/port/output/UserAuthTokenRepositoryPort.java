package edu.market.userservice.domain.port.output;

import edu.market.userservice.domain.model.UserAuthToken;

import java.util.List;
import java.util.Optional;

/**
 * Puerto para operaciones de repositorio de tokens de autenticación
 */
public interface UserAuthTokenRepositoryPort {
    
    /**
     * Guarda un token de autenticación
     * @param token Token a guardar
     * @return Token guardado
     */
    UserAuthToken save(UserAuthToken token);
    
    /**
     * Busca un token por su valor
     * @param token Valor del token
     * @return Token encontrado o vacío
     */
    Optional<UserAuthToken> findByToken(String token);
    
    /**
     * Busca un token por su refresh token
     * @param refreshToken Valor del refresh token
     * @return Token encontrado o vacío
     */
    Optional<UserAuthToken> findByRefreshToken(String refreshToken);
    
    /**
     * Busca todos los tokens activos de un usuario
     * @param userId ID del usuario
     * @return Lista de tokens activos
     */
    List<UserAuthToken> findActiveTokensByUserId(Long userId);
    
    /**
     * Revoca todos los tokens de un usuario
     * @param userId ID del usuario
     * @return Número de tokens revocados
     */
    int revokeAllTokensByUserId(Long userId);
    
    /**
     * Revoca un token específico
     * @param token Valor del token a revocar
     * @return 1 si se revocó correctamente, 0 si no se encontró
     */
    int revokeToken(String token);
    
    /**
     * Busca un token social activo para un usuario y proveedor
     * @param userId ID del usuario
     * @param provider Proveedor social (google, facebook, etc.)
     * @return Token encontrado o vacío
     */
    Optional<UserAuthToken> findActiveSocialToken(Long userId, String provider);
}
