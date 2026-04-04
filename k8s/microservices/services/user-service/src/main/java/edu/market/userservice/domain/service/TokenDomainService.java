package edu.market.userservice.domain.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.market.userservice.domain.mapper.TokenDomainMapper;
import edu.market.userservice.domain.model.User;
import edu.market.userservice.domain.model.UserAuthToken;
import edu.market.userservice.domain.port.output.TokenDomainServicePort;
import edu.market.userservice.domain.port.output.UserAuthTokenRepositoryPort;
import edu.market.userservice.domain.vo.TokenCreateVO;
import edu.market.userservice.domain.vo.TokenRefreshVO;
import edu.market.userservice.infrastructure.config.TokenConfig;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

/**
 * Implementación del servicio de dominio para la gestión de tokens
 */
public class TokenDomainService implements TokenDomainServicePort {

    private final Logger log = LoggerFactory.getLogger(TokenDomainService.class);
    private final TokenConfig tokenConfig;
    private final UserAuthTokenRepositoryPort userAuthTokenRepository;

    public TokenDomainService(TokenConfig tokenConfig,
            UserAuthTokenRepositoryPort userAuthTokenRepository) {
        this.tokenConfig = tokenConfig;
        this.userAuthTokenRepository = userAuthTokenRepository;
    }

    /**
     * Crea un objeto UserAuthToken con la información necesaria
     *
     * @param tokenCreateVO Value Object con la información para crear el token
     * @return Objeto UserAuthToken
     */
    @Override
    public UserAuthToken createUserAuthToken(TokenCreateVO tokenCreateVO) {
        log.debug("Creando objeto UserAuthToken con token: {} y refresh token: {}",
                tokenCreateVO.getAccessToken()
                        .substring(0, Math.min(8, tokenCreateVO.getAccessToken().length())) + "...",
                tokenCreateVO.getRefreshToken()
                        .substring(0, Math.min(8, tokenCreateVO.getRefreshToken().length()))
                        + "...");

        // Utilizar el mapper para crear el token
        UserAuthToken token = TokenDomainMapper.toUserAuthToken(tokenCreateVO);

        log.debug("Objeto UserAuthToken creado exitosamente");
        return token;
    }

    /**
     * Prepara los claims para un token de acceso
     *
     * @param user Usuario para el que se genera el token
     * @return Mapa de claims
     */
    @Override
    public Map<String, Object> prepareAccessTokenClaims(User user) {
        log.debug("Preparando claims para el token de acceso del usuario con ID: {}", user.getId());

        Map<String, Object> claims = new java.util.HashMap<>();
        claims.put("id", user.getId());
        claims.put("email", user.getEmail());

        // Añadir roles si existen
        if (user.getRoles() != null && !user.getRoles().isEmpty()) {
            claims.put("roles", user.getRoles().stream()
                    .map(role -> role.getName())
                    .toList());
            log.debug("Añadidos {} roles a los claims del token", user.getRoles().size());
        } else {
            log.debug("No se encontraron roles para añadir a los claims del token");
        }

        log.debug("Claims para el token de acceso preparados exitosamente");
        return claims;
    }

    /**
     * Prepara los claims para un token de actualización
     *
     * @param user Usuario para el que se genera el token
     * @return Mapa de claims
     */
    @Override
    public Map<String, Object> prepareRefreshTokenClaims(User user) {
        log.debug("Preparando claims para el refresh token del usuario con ID: {}", user.getId());

        String tokenId = java.util.UUID.randomUUID().toString();
        log.debug("Generado ID único para el refresh token: {}", tokenId);

        Map<String, Object> claims = new java.util.HashMap<>();
        claims.put("id", user.getId());
        claims.put("tokenId", tokenId);

        log.debug("Claims para el refresh token preparados exitosamente");
        return claims;
    }

    /**
     * Genera un token de autenticación para un usuario
     *
     * @param user Usuario para el que se genera el token
     * @return Token de autenticación generado
     */
    @Override
    public UserAuthToken generateToken(User user) {

        log.info("init - generateToken");
        log.debug("generateToken for user: {}", user.getId());

        // Utilizar el mapper para generar el token con los parámetros de configuración
        UserAuthToken token = TokenDomainMapper.generateUserAuthToken(
                user,
                tokenConfig.getAccessTokenDurationMinutes(),
                tokenConfig.getRefreshTokenDurationDays()
        );

        log.info("end - generateToken");
        return token;
    }

    /**
     * Refresca un token de autenticación existente
     *
     * @param tokenRefreshVO
     * @return Token refrescado
     */
    @Override
    public UserAuthToken refreshToken(TokenRefreshVO tokenRefreshVO) {

        log.info("init - refreshToken");

        UserAuthToken existingToken = tokenRefreshVO.getExistingToken();

        if (existingToken == null) {
            log.error("refreshToken error: Token nulo");
            throw new IllegalArgumentException("El token no puede ser nulo");
        }

        log.debug("refreshToken for user: {}", existingToken.getUserId());

        // Generar un nuevo token de acceso
        String newAccessToken = java.util.UUID.randomUUID().toString();

        // Establecer el nuevo tiempo de expiración según la configuración
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime accessTokenExpiry = now.plusMinutes(
                tokenConfig.getAccessTokenDurationMinutes());

        log.debug("refreshToken setting new expiration time: {}", accessTokenExpiry);

        // Actualizar el token existente
        existingToken.setAccessToken(newAccessToken);
        existingToken.setExpiresAt(accessTokenExpiry);
        existingToken.setUpdatedAt(now);
        existingToken.setIpAddress(tokenRefreshVO.getIpAddress());
        existingToken.setUserAgent(tokenRefreshVO.getUserAgent());

        log.info("end - refreshToken");
        return existingToken;
    }


    /**
     * Revoca un token específico
     *
     * @param token Token a revocar (puede ser access token o refresh token)
     * @return true si el token fue revocado correctamente
     */
    @Override
    public boolean revokeToken(String token) {

        log.info("init - revokeToken");

        if (token == null || token.isEmpty()) {
            log.error("revokeToken error: Token nulo o vacío");
            return false;
        }

        // Truncar el token para el log por seguridad
        String tokenPrefix = token.length() > 8 ? token.substring(0, 8) + "..." : "[token corto]";
        log.debug("revokeToken for token: {}", tokenPrefix);

        // La lógica de revocación real se delega al repositorio
        // Este método actúa como una fachada para mantener la coherencia del dominio
        int result = userAuthTokenRepository.revokeToken(token);

        boolean success = result > 0;
        log.debug("revokeToken result: {}", success ? "revocado" : "no encontrado");

        log.info("end - revokeToken");
        return success;
    }

    /**
     * Revoca todos los tokens de un usuario
     *
     * @param userId ID del usuario cuyos tokens serán revocados
     * @return Número de tokens revocados
     */
    @Override
    public int revokeAllTokensForUser(Long userId) {

        log.info("init - revokeAllTokensForUser");

        if (userId == null) {
            log.error("revokeAllTokensForUser error: UserId nulo");
            return 0;
        }

        log.debug("revokeAllTokensForUser for user: {}", userId);

        // La lógica de revocación real se delega al repositorio
        // Este método actúa como una fachada para mantener la coherencia del dominio
        int result = userAuthTokenRepository.revokeAllTokensByUserId(userId);

        log.debug("revokeAllTokensForUser result: {} tokens revocados", result);

        log.info("end - revokeAllTokensForUser");
        return result;
    }

    /**
     * Busca un token activo para un usuario y proveedor social
     *
     * @param userId   ID del usuario
     * @param provider Proveedor social (ej. "GOOGLE", "FACEBOOK")
     * @return Token activo si existe
     */
    @Override
    public Optional<UserAuthToken> findActiveSocialToken(Long userId, String provider) {

        log.info("init - findActiveSocialToken");

        if (userId == null || provider == null) {
            log.error("findActiveSocialToken error: UserId o provider nulo");
            return Optional.empty();
        }

        log.debug("findActiveSocialToken for user: {} and provider: {}", userId, provider);

        // Delegar la búsqueda al repositorio
        Optional<UserAuthToken> token = userAuthTokenRepository.findActiveSocialToken(userId,
                provider);

        if (token.isPresent()) {
            log.debug("findActiveSocialToken found active token for user: {}", userId);
        } else {
            log.debug("findActiveSocialToken no active token found for user: {}", userId);
        }

        log.info("end - findActiveSocialToken");
        return token;
    }
}
