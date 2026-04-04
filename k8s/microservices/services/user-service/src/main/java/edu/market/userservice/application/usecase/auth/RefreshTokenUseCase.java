package edu.market.userservice.application.usecase.auth;

import edu.market.userservice.application.port.input.RefreshTokenUseCasePort;
import edu.market.userservice.domain.model.UserAuthToken;
import edu.market.userservice.domain.port.output.TokenDomainServicePort;
import edu.market.userservice.domain.port.output.UserAuthTokenRepositoryPort;
import edu.market.userservice.domain.vo.TokenRefreshRequestVO;
import edu.market.userservice.domain.vo.TokenRefreshVO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Caso de uso para refrescar un token de autenticación
 */
public class RefreshTokenUseCase implements RefreshTokenUseCasePort {
    
    private final Logger log = LoggerFactory.getLogger(RefreshTokenUseCase.class);
    private final UserAuthTokenRepositoryPort userAuthTokenRepository;
    private final TokenDomainServicePort tokenDomainService;

    public RefreshTokenUseCase(
            UserAuthTokenRepositoryPort userAuthTokenRepository,
            TokenDomainServicePort tokenDomainService) {
        this.userAuthTokenRepository = userAuthTokenRepository;
        this.tokenDomainService = tokenDomainService;
    }

    /**
     * Refresca un token de autenticación usando un refresh token
     * 
     * @param refreshRequest Value Object con los datos de la solicitud de renovación
     * @return Token de autenticación actualizado o vacío si el refresh token es inválido o ha expirado
     */
    @Override
    public Optional<UserAuthToken> execute(TokenRefreshRequestVO refreshRequest) {
        try {
            log.info("init - execute refresh token");
            
            // Obtener el token limpio (sin prefijo Bearer si existe)
            String cleanRefreshToken = refreshRequest.getCleanRefreshToken();
            log.debug("execute refresh token with clean token: {}", cleanRefreshToken.substring(0, 8) + "...");
            
            // Buscar el token por refresh token
            Optional<UserAuthToken> tokenOpt = userAuthTokenRepository.findByRefreshToken(cleanRefreshToken);
            
            if (tokenOpt.isEmpty()) {
                log.warn("execute refresh token not found");
                return Optional.empty();
            }
            
            UserAuthToken token = tokenOpt.get();
            log.debug("execute refresh token found for user: {}", token.getUserId());
            
            // Verificar que el refresh token no ha expirado
            if (token.getRefreshExpiresAt().isBefore(LocalDateTime.now())) {
                log.warn("execute refresh token expired for user: {}", token.getUserId());
                return Optional.empty();
            }
            
            // Verificar que el token está activo
            if (!"ACTIVE".equals(token.getStatus())) {
                log.warn("execute refresh token not active for user: {}, status: {}", 
                        token.getUserId(), token.getStatus());
                return Optional.empty();
            }
            
            // Refrescar el token existente usando el servicio de dominio
            log.debug("execute refreshing token for user: {}", token.getUserId());
            
            // Usar el servicio de dominio para refrescar el token existente
            UserAuthToken refreshedToken = tokenDomainService.refreshToken(new TokenRefreshVO(
                    token, 
                    refreshRequest.getIpAddress(), 
                    refreshRequest.getUserAgent()));
            
            // Guardar el token actualizado
            UserAuthToken savedToken = userAuthTokenRepository.save(refreshedToken);
            log.info("end - execute refresh token");
            return Optional.of(savedToken);
        } catch (Exception e) {
            log.error("execute refresh token error: {}", e.getMessage(), e);
            return Optional.empty();
        }
    }
}
