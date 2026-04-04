package edu.market.userservice.application.usecase.auth;

import edu.market.userservice.application.port.input.LogoutUserUseCasePort;
import edu.market.userservice.domain.port.output.TokenDomainServicePort;
import edu.market.userservice.domain.port.output.UserRepositoryPort;
import edu.market.userservice.domain.vo.LogoutAllRequestVO;
import edu.market.userservice.domain.vo.LogoutRequestVO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Caso de uso para cerrar la sesión de un usuario
 */
public class LogoutUserUseCase implements LogoutUserUseCasePort {

    private final Logger log = LoggerFactory.getLogger(LogoutUserUseCase.class);
    
    private final TokenDomainServicePort tokenDomainService;    
    private final UserRepositoryPort userRepository;
    
    public LogoutUserUseCase(TokenDomainServicePort tokenDomainService, UserRepositoryPort userRepository) {
        this.tokenDomainService = tokenDomainService;
        this.userRepository = userRepository;
    }

    /**
     * Cierra la sesión de un usuario revocando sus tokens
     * 
     * @param logoutRequest Value Object con la información de cierre de sesión
     * @return true si la sesión fue cerrada correctamente
     */
    @Override    
    public boolean execute(LogoutRequestVO logoutRequest) {
        try {
            log.info("init - execute logout");
            
            // Obtener los tokens limpios (sin el prefijo "Bearer ")
            String accessToken = logoutRequest.getCleanAccessToken();
            
            log.debug("execute revoking access token");
            // Revocar el token de acceso usando el servicio de dominio
            boolean accessTokenResult = tokenDomainService.revokeToken(accessToken);
            
            // Revocar el token de actualización si está presente
            boolean refreshTokenResult = false;
            if (logoutRequest.hasRefreshToken()) {
                log.debug("execute revoking refresh token");
                refreshTokenResult = tokenDomainService.revokeToken(logoutRequest.getCleanRefreshToken());
            }
            
            // Registrar la información de cierre de sesión
            log.debug("execute logout successful: accessToken={}, refreshToken={}", 
                    accessTokenResult, refreshTokenResult);
            
            log.info("end - execute logout");
            return accessTokenResult || refreshTokenResult;
        } catch (Exception e) {
            log.error("execute logout error: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Cierra todas las sesiones de un usuario revocando todos sus tokens de acceso
     * 
     * @param logoutAllRequest Value Object con la información del usuario cuyas sesiones se cerrarán
     * @return Número de tokens revocados
     * @throws IllegalArgumentException si el usuario no existe
     */
    @Override    
    public int execute(LogoutAllRequestVO logoutAllRequest) {
        try {
            log.info("init - execute logout all");
            
            Long userId = logoutAllRequest.getUserId();
            log.debug("execute logout all for user: {}", userId);
            
            // Verificar que el usuario existe
            if (!userRepository.findById(userId).isPresent()) {
                log.error("execute logout all error: Usuario no encontrado con ID: {}", userId);
                throw new IllegalArgumentException("Usuario no encontrado con ID: " + userId);
            }
            
            // Revocar todos los tokens del usuario usando el servicio de dominio
            int result = tokenDomainService.revokeAllTokensForUser(userId);
            log.debug("execute logout all revoked {} tokens for user: {}", result, userId);
            
            log.info("end - execute logout all");
            return result;
        } catch (Exception e) {
            log.error("execute logout all error: {}", e.getMessage(), e);
            throw e;
        }
    }
}
