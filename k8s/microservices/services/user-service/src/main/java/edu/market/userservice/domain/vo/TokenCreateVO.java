package edu.market.userservice.domain.vo;

import java.time.LocalDateTime;

/**
 * Value Object para la creación de tokens de autenticación
 */
public final class TokenCreateVO {
    
    private final String accessToken;
    private final String refreshToken;
    private final LocalDateTime expiresAt;
    private final LocalDateTime refreshExpiresAt;
    
    /**
     * Constructor para TokenCreateVO
     * 
     * @param accessToken Token de acceso
     * @param refreshToken Token de actualización
     * @param expiresAt Fecha de expiración del token de acceso
     * @param refreshExpiresAt Fecha de expiración del token de actualización
     */
    public TokenCreateVO(String accessToken, String refreshToken, 
                        LocalDateTime expiresAt, LocalDateTime refreshExpiresAt) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.expiresAt = expiresAt;
        this.refreshExpiresAt = refreshExpiresAt;
    }
    
    public String getAccessToken() {
        return accessToken;
    }
    
    public String getRefreshToken() {
        return refreshToken;
    }
    
    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }
    
    public LocalDateTime getRefreshExpiresAt() {
        return refreshExpiresAt;
    }
}
