package edu.market.userservice.domain.vo;

/**
 * Value Object para la solicitud de cierre de sesión
 * Implementado como un objeto inmutable siguiendo las mejores prácticas de DDD
 */
public final class LogoutRequestVO {
    
    private final String accessToken;
    private final String refreshToken;
    private final String ipAddress;
    private final String userAgent;
    
    public String getAccessToken() {
        return accessToken;
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
     * Constructor para la solicitud de cierre de sesión
     * 
     * @param accessToken Token de acceso a revocar
     * @param refreshToken Token de actualización a revocar
     * @param ipAddress Dirección IP del cliente
     * @param userAgent Agente de usuario del cliente
     */
    public LogoutRequestVO(String accessToken, String refreshToken, String ipAddress, String userAgent) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
    }
    
    /**
     * Verifica si el token de acceso comienza con el prefijo "Bearer "
     * y lo extrae si es necesario
     * 
     * @return Token de acceso sin el prefijo "Bearer "
     */
    public String getCleanAccessToken() {
        if (accessToken != null && accessToken.startsWith("Bearer ")) {
            return accessToken.substring(7);
        }
        return accessToken;
    }
    
    /**
     * Verifica si el token de actualización comienza con el prefijo "Bearer "
     * y lo extrae si es necesario
     * 
     * @return Token de actualización sin el prefijo "Bearer "
     */
    public String getCleanRefreshToken() {
        if (refreshToken != null && refreshToken.startsWith("Bearer ")) {
            return refreshToken.substring(7);
        }
        return refreshToken;
    }
    
    /**
     * Verifica si el token de actualización está presente
     * 
     * @return true si el token de actualización está presente
     */
    public boolean hasRefreshToken() {
        return refreshToken != null && !refreshToken.isEmpty();
    }
}
