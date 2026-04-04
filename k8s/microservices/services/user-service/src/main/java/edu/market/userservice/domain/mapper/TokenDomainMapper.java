package edu.market.userservice.domain.mapper;

import edu.market.userservice.domain.enums.TokenStatusType;
import edu.market.userservice.domain.model.User;
import edu.market.userservice.domain.model.UserAuthToken;
import edu.market.userservice.domain.vo.TokenCreateVO;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Mapper para entidades de dominio relacionadas con tokens
 */
public class TokenDomainMapper {
    
    /**
     * Crea un objeto UserAuthToken a partir de un TokenCreateVO
     * 
     * @param tokenCreateVO Value Object con los datos para crear el token
     * @return Objeto UserAuthToken
     */
    public static UserAuthToken toUserAuthToken(TokenCreateVO tokenCreateVO) {
        UserAuthToken token = new UserAuthToken();
        token.setAccessToken(tokenCreateVO.getAccessToken());
        token.setRefreshToken(tokenCreateVO.getRefreshToken());
        token.setExpiresAt(tokenCreateVO.getExpiresAt());
        token.setRefreshExpiresAt(tokenCreateVO.getRefreshExpiresAt());
        token.setCreatedAt(LocalDateTime.now());
        token.setUpdatedAt(LocalDateTime.now());
        
        return token;
    }
    
    /**
     * Crea un TokenCreateVO con los parámetros necesarios para generar un token
     * 
     * @param accessToken Token de acceso
     * @param refreshToken Token de actualización
     * @param expiresAt Fecha de expiración del token de acceso
     * @param refreshExpiresAt Fecha de expiración del token de actualización
     * @return TokenCreateVO con los datos para crear un token
     */
    public static TokenCreateVO createTokenVO(String accessToken, String refreshToken, 
                                            LocalDateTime expiresAt, LocalDateTime refreshExpiresAt) {
        return new TokenCreateVO(accessToken, refreshToken, expiresAt, refreshExpiresAt);
    }
    
    /**
     * Genera un token de autenticación para un usuario con tiempos de expiración calculados
     * 
     * @param user Usuario para el que se genera el token
     * @param accessTokenDurationMinutes Duración del token de acceso en minutos
     * @param refreshTokenDurationDays Duración del token de refresco en días
     * @return Token de autenticación generado
     */
    public static UserAuthToken generateUserAuthToken(User user, int accessTokenDurationMinutes, int refreshTokenDurationDays) {
        // Generar tokens únicos
        String accessToken = UUID.randomUUID().toString();
        String refreshToken = UUID.randomUUID().toString();
        
        // Establecer tiempos de expiración
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime accessTokenExpiry = now.plusMinutes(accessTokenDurationMinutes);
        LocalDateTime refreshTokenExpiry = now.plusDays(refreshTokenDurationDays);
        
        // Crear el TokenCreateVO
        TokenCreateVO tokenCreateVO = createTokenVO(accessToken, refreshToken, accessTokenExpiry, refreshTokenExpiry);
        
        // Crear el token usando el VO
        UserAuthToken token = toUserAuthToken(tokenCreateVO);
        token.setUserId(user.getId());
        token.setStatus(TokenStatusType.ACTIVE);
        
        return token;
    }
}