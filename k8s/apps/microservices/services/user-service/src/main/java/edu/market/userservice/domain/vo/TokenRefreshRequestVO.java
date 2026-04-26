package edu.market.userservice.domain.vo;

import java.util.Objects;

/**
 * Value Object para representar una solicitud de renovación de token
 * Implementado como un objeto inmutable siguiendo las mejores prácticas de DDD
 */
public final class TokenRefreshRequestVO {
    
    private final String refreshToken;
    private final String ipAddress;
    private final String userAgent;
    
    /**
     * Constructor para la solicitud de renovación de token
     * 
     * @param refreshToken Token de renovación
     * @param ipAddress Dirección IP del cliente
     * @param userAgent Agente de usuario del cliente
     */
    public TokenRefreshRequestVO(String refreshToken, String ipAddress, String userAgent) {
        // Validaciones
        if (refreshToken == null || refreshToken.trim().isEmpty()) {
            throw new IllegalArgumentException("El token de renovación no puede estar vacío");
        }
        
        this.refreshToken = refreshToken;
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
    }
    
    public String getRefreshToken() {
        return refreshToken;
    }
    
    public String getIpAddress() {
        return ipAddress;
    }
    
    public String getUserAgent() {
        return userAgent;
    }
    
    /**
     * Verifica si el token de renovación comienza con el prefijo "Bearer "
     * y lo extrae si es necesario
     * 
     * @return Token de renovación sin el prefijo "Bearer "
     */
    public String getCleanRefreshToken() {
        if (refreshToken != null && refreshToken.startsWith("Bearer ")) {
            return refreshToken.substring(7);
        }
        return refreshToken;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TokenRefreshRequestVO that = (TokenRefreshRequestVO) o;
        return Objects.equals(refreshToken, that.refreshToken) &&
               Objects.equals(ipAddress, that.ipAddress) &&
               Objects.equals(userAgent, that.userAgent);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(refreshToken, ipAddress, userAgent);
    }
    
    @Override
    public String toString() {
        return "TokenRefreshRequestVO{" +
                "refreshToken='[PROTECTED]'" +
                ", ipAddress='" + ipAddress + '\'' +
                ", userAgent='" + userAgent + '\'' +
                '}';
    }
}
