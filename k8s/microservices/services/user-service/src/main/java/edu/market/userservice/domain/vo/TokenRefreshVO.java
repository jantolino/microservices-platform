package edu.market.userservice.domain.vo;

import edu.market.userservice.domain.model.UserAuthToken;

/**
 * Value Object para el refresco de tokens de autenticación
 */
public final class TokenRefreshVO {
    
    private final UserAuthToken existingToken;
    private final String ipAddress;
    private final String userAgent;
    
    /**
     * Constructor para TokenRefreshVO
     * 
     * @param existingToken Token existente a refrescar
     * @param ipAddress Nueva dirección IP del usuario
     * @param userAgent Nuevo user-agent del usuario
     */
    public TokenRefreshVO(UserAuthToken existingToken, String ipAddress, String userAgent) {
        this.existingToken = existingToken;
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
    }
    
    public UserAuthToken getExistingToken() {
        return existingToken;
    }
    
    public String getIpAddress() {
        return ipAddress;
    }
    
    public String getUserAgent() {
        return userAgent;
    }
}
